package com.fintech.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.Dto.InvoiceEmailEvent;
import com.fintech.Service.InvoiceService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class InvoiceEmailProducer {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceEmailProducer.class);
    private static final String broker_url = "tcp://localhost:61616";
    private static final String queue="email.queue";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendInvoiceEmailEvent(InvoiceEmailEvent event) {

        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(broker_url);

            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


            Destination destination = session.createQueue(queue);


            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            String json = objectMapper.writeValueAsString(event);

            TextMessage message = session.createTextMessage(json);

            producer.send(message);

            producer.close();
            session.close();
            connection.close();

            logger.info("Invoice event is sent to the queue for invoice id: {}",event.getInvoiceId());

        } catch (Exception e) {
            logger.error("Error occurred while sending invoice event to the queue for invoice id: {}",event.getInvoiceId(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}