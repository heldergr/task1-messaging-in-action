package org.example.flight.checkin.checkinapp;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import org.example.flight.checkin.model.Passenger;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckinApp {
    public static void main(String[] args) throws NamingException, JMSException, InterruptedException {
        final var initialContext = new InitialContext();
        final var requestQueue = (Queue) initialContext.lookup("queue/requestQueue");
        final var replyQueue = (Queue) initialContext.lookup("queue/replyQueue");

        try (final var activeMQConnectionFactory = new ActiveMQConnectionFactory();
            final var jmsContext = activeMQConnectionFactory.createContext()) {
            final var jmsProducer = jmsContext.createProducer();
            final var responses = new HashMap<String, Boolean>();

            for (final Passenger passenger: createPassengers()) {
                ObjectMessage objectMessage = jmsContext.createObjectMessage();
                objectMessage.setObject(passenger);
                objectMessage.setJMSReplyTo(replyQueue);
                jmsProducer.send(requestQueue, objectMessage);
                responses.put(objectMessage.getJMSMessageID(), null);
            }

//            Thread.sleep(40000);

            final var consumer1 = jmsContext.createConsumer(replyQueue);
            final var consumer2 = jmsContext.createConsumer(replyQueue);

            for (int i=0; i<10; i+=2) {
                final MapMessage message1 = (MapMessage) consumer1.receive(30000);
                responses.put(message1.getJMSCorrelationID(), message1.getBoolean("reserved"));

                final MapMessage message2 = (MapMessage) consumer2.receive(30000);
                responses.put(message2.getJMSCorrelationID(), message2.getBoolean("reserved"));
            }

            System.out.println("All reservations status per message: ");
            System.out.println(responses);
        }
    }

    private static List<Passenger> createPassengers() {
        return Map.of("Edson", "Pele",
                "Diego", "Maradona",
                "Michel",  "Platini",
                "Paolo", "Maldini",
                "Emilio", "Butragueno",
                "Hugo", "Sanchez",
                "Ivan", "Zamorano",
                "Enzo", "Franchescoli",
                "Marco Antonio", "Etcheverry",
                "Vincenzo", "Scifo"
        ).entrySet().stream().map(entry -> CheckinApp.createPassenger(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    private static Passenger createPassenger(final String firstName, final String lastName) {
        return new Passenger(
              "id for " + firstName + lastName,
                firstName, lastName,
                String.format("%s.%s@gmail.com", firstName, lastName),
                "+55 31 123455"
        );
    }
}
