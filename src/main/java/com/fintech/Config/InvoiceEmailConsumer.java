package com.fintech.Config;

import javax.jms.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.Dto.InvoiceEmailEvent;
import com.fintech.Model.Entity.Invoice;
import com.fintech.Service.InvoiceService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceEmailConsumer  {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceEmailConsumer.class);
    private static final String broker_url = "tcp://localhost:61616";
    private static final String queue="email.queue";

    private final  EmailService emailService;
    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvoiceEmailConsumer(EmailService emailService, InvoiceService invoiceService) {
        this.emailService = emailService;
        this.invoiceService = invoiceService;
    }

    public  void start() {

        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(broker_url);

            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue(queue);

            MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(message ->{
                try {
                    if (message instanceof TextMessage) {

                        String json = ((TextMessage)message).getText();

                        InvoiceEmailEvent event = objectMapper.readValue(json,InvoiceEmailEvent.class);

                        Invoice invoice = invoiceService.findById(event.getInvoiceId());

                        emailService.sendInvoiceEmail(
                                invoice,
                                event.getRecipientEmail(),
                                event.getPaymentLink(),
                                event.getQrPath()
                        );
                    }

                } catch (Exception e) {
                    logger.error("Error occurred while reading Invoice event", e);
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            logger.error("Error listening to the queue for invoice event", e);
            throw new RuntimeException("Consumer failed to start", e);
        }
    }
}
