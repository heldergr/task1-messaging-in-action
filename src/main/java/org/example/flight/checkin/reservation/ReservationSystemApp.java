package org.example.flight.checkin.reservation;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ReservationSystemApp {

    public static void main(String[] args) throws NamingException, InterruptedException {
        final var initialContext = new InitialContext();
        final var requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

        try (final var activeMQConnectionFactory = new ActiveMQConnectionFactory();
             final var jmsContext = activeMQConnectionFactory.createContext()) {
            final var consumer1 = jmsContext.createConsumer(requestQueue);
            consumer1.setMessageListener(new ReservationSystemListener());
            Thread.sleep(30000);
        }
    }
}
