package com.fintech.Service;

import com.fintech.Config.InvoiceEmailProducer;
import com.fintech.Controller.AdminController;
import com.fintech.Dao.InvoiceDao;
import com.fintech.Dao.TransactionDao;
import com.fintech.Dao.WalletDao;
import com.fintech.Dto.InvoiceEmailEvent;
import com.fintech.Exception.BadRequestException;
import com.fintech.Exception.InternalServerException;
import com.fintech.Exception.NotFoundException;
import com.fintech.Model.Entity.Invoice;
import com.fintech.Model.Entity.Wallet;
import com.fintech.Model.Enum.InvoiceStatus;
import com.fintech.Model.Enum.TransactionStatus;
import com.fintech.Model.Enum.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class InvoiceService {

    private final DataSource dataSource;
    private final InvoiceDao invoiceDao;
    private final WalletDao walletDao;
    private final TransactionDao transactionDao;
    private final QrService qrService;
    private final String qrPath;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);


    public InvoiceService(DataSource dataSource, QrService qrService, String qrPath) {
        this.dataSource = dataSource;
        this.invoiceDao = new InvoiceDao();
        this.walletDao=new WalletDao(dataSource);
        this.transactionDao=new TransactionDao(dataSource);
        this.qrService=qrService;
        this.qrPath=qrPath;

    }

    public Map<String, Object> createInvoice(Long userId, BigDecimal amount, String description) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Amount must be positive : {}", amount);
            throw new BadRequestException("Amount must be positive");
        }

        try (Connection connection = dataSource.getConnection()) {

            String paymentToken = UUID.randomUUID().toString();

            String paymentUrl = "https://localhost:8080/invoice/pay?token=" + paymentToken;

            String qrPath = qrService.generateQr(paymentUrl);

            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(30);

            Long invoiceId = invoiceDao.createInvoice(connection,
                    userId,
                    amount,
                    description,
                    paymentToken,
                    qrPath,
                    expiryTime
            );

            logger.info("Invoice created with ID: {}",invoiceId);

            return Map.of(
                    "invoiceId", invoiceId,
                    "amount", amount,
                    "status", InvoiceStatus.UNPAID.name(),
                    "paymentToken", paymentToken,
                    "qrPath", qrPath,
                    "expiryTime", expiryTime
            );

        } catch (Exception e) {
            logger.error("Error Occurred while creating invoice for user id: {} , description: {}",userId,description, e);
            throw new InternalServerException("Failed to create invoice");
        }
    }

    public Map<String, Object> payInvoice(Long payerId, String paymentToken) {

        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(false);

            try{

                Invoice invoice = invoiceDao.getByTokenForUpdate(connection, paymentToken);

                if (invoice == null) {
                    logger.warn("Invoice cannot be found for token: {}",paymentToken);
                    throw new BadRequestException("Invalid invoice");
                }

                if (invoice.getStatus() == InvoiceStatus.PAID) {
                    logger.warn("Invoice already paid for token: {}",paymentToken);
                    throw new BadRequestException("Invoice already paid");
                }

                if (invoice.getExpiryTime().isBefore(LocalDateTime.now())) {

                    invoiceDao.updateStatus(connection, invoice.getId(), InvoiceStatus.EXPIRED);

                    connection.commit();
                    logger.warn("Invoice payment time is expired for token: {}",paymentToken);
                    throw new BadRequestException("Invoice expired");
                }

                Wallet payerWallet = walletDao.findByUserIdForUpdate(connection, payerId);

                if (payerWallet.getBalance().compareTo(invoice.getAmount()) < 0) {
                    logger.warn("Sender's wallet amount is not Sufficient for transfer for this amount: {}",invoice.getAmount());
                    throw new BadRequestException("Insufficient balance");
                }

                Wallet receiverWallet = walletDao.findByUserIdForUpdate(connection, invoice.getUserId());

                BigDecimal newPayerBalance = payerWallet.getBalance().subtract(invoice.getAmount());

                BigDecimal newReceiverBalance = receiverWallet.getBalance().add(invoice.getAmount());

                walletDao.updateBalance(connection, payerId, newPayerBalance);

                walletDao.updateBalance(connection, receiverWallet.getUserId(), newReceiverBalance);

                long transaction_id= transactionDao.createTransaction(connection, payerId, invoice.getUserId(),
                        invoice.getAmount(), TransactionType.INVOICE_PAYMENT);
                transactionDao.updateInvoiceId(connection,transaction_id,invoice.getId());
                transactionDao.updateStatus(connection, transaction_id,TransactionStatus.SUCCESS.name());

                invoiceDao.updateStatus(connection, invoice.getId(), InvoiceStatus.PAID);

                connection.commit();
                logger.info("Invoice paid successfully with transaction ID: {}",transaction_id);
                return Map.of(
                        "message", "Invoice paid successfully",
                        "amount", invoice.getAmount()
                );
            }
            catch (Exception e){
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error occurred while paying invoice for token: {}",paymentToken, e);
            throw new InternalServerException("Payment failed");
        }
    }

    public Invoice findById(Long id){
        try(Connection connection = dataSource.getConnection()){
            Invoice invoice=invoiceDao.findById(connection,id);
            if(invoice==null){
                logger.warn("Invoice not found for ID: {}",id);
                throw new NotFoundException("Invoice not found");
            }
            return invoice;
        }catch (SQLException e){
            logger.error("Error Occurred while finding invoice with ID: {}",id, e);
            throw new RuntimeException("Error finding the data");
        }


    }

    public void sendInvoiceEmail(Long userId, Long invoiceId, String recipientEmail) {

        try (Connection connection = dataSource.getConnection()) {

            Invoice invoice = invoiceDao.findById(connection, invoiceId);

            if (invoice == null) {
                logger.warn("Invoice not found for id: {}",invoiceId);
                throw new BadRequestException("Invoice not found");
            }

            if (!invoice.getUserId().equals(userId)) {
                logger.warn("User id: {} not authorized to access this invoice with ID: {}",userId,invoiceId);
                throw new BadRequestException("Not authorized");
            }

            String paymentLink = "http://localhost:8080/invoice/pay?token=" + invoice.getPaymentToken();

            String qrAbsolutePath =qrPath+ File.separator+new File(invoice.getQrCodePath()).getName();

            InvoiceEmailEvent event = new InvoiceEmailEvent();
            event.setInvoiceId(invoice.getId());
            event.setRecipientEmail(recipientEmail);
            event.setPaymentLink(paymentLink);
            event.setQrPath(qrAbsolutePath);

            InvoiceEmailProducer producer = new InvoiceEmailProducer();
            producer.sendInvoiceEmailEvent(event);

            logger.info("Invoice event is sent to the Active MQ producer for invoice id: {}",invoiceId);

        }catch (BadRequestException e){
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to send invoice email", e);
        }
    }
}