package com.fintech.Config;

import com.fintech.Model.Entity.Invoice;
import com.fintech.Service.InvoiceService;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    final String from="shopfinityecommercem@gmail.com";
    final String password="znmvxkokllynpbub";

    public void sendInvoiceEmail(Invoice invoice,
                                 String recipientEmail,
                                 String paymentLink,
                                 String qrAbsolutePath) {

        try {

            //Template processing
            Template template = TemplateConfig.getConfiguration().getTemplate("Invoice.ftl");

            Map<String, Object> model = new HashMap<>();
            model.put("amount", invoice.getAmount());
            model.put("description", invoice.getDescription());
            model.put("paymentLink", paymentLink);

            StringWriter writer = new StringWriter();
            template.process(model, writer);
            String htmlContent = writer.toString();

            //Mail Processing
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(
                    props,
                    new Authenticator() {
                        protected PasswordAuthentication
                        getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    from,
                                    password
                            );
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );
            message.setSubject("Invoice Payment Request");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(qrAbsolutePath));
            attachmentPart.setFileName("invoice-qr.png");

            Multipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

            logger.info("Invoice for id {} is successfully sent to email : {}",invoice.getId(),recipientEmail);

        } catch (Exception e) {
            logger.error("Failed to send a email for invoice id: {}",invoice.getId(),e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
