package org.example.flight.checkin.reservation;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.example.flight.checkin.model.Passenger;

import javax.jms.*;

public class ReservationSystemListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        final ObjectMessage objectMessage = (ObjectMessage) message;
        try (final var activeMQConnectionFactory = new ActiveMQConnectionFactory();
             final var jmsContext = activeMQConnectionFactory.createContext()) {
            final Passenger passenger = (Passenger) objectMessage.getObject();
            final String firstName = passenger.getFirstName();
            final boolean reserved = (firstName.equals("Edson") || firstName.equals("Hugo"));
            System.out.println("Reservation request for " + passenger);

            final MapMessage mapMessage = jmsContext.createMapMessage();
            mapMessage.setBoolean("reserved", reserved);
            mapMessage.setJMSCorrelationID(message.getJMSMessageID());
            final JMSProducer jmsProducer = jmsContext.createProducer();
            jmsProducer.send(message.getJMSReplyTo(), mapMessage);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
