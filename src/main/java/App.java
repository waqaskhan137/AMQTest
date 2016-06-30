/**
 * Created by Rana on 6/30/2016.
 */

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Hello world!
 */
public class App {

    public static final String url = "failover://(tcp://localhost:61626,tcp://localhost:61616)?timeout=3000&randomize=false";


    public static void main(String[] args) throws Exception {
        try {
            thread(new HelloWorldProducer(), false);
            Thread.sleep(1000);

          /*  thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            Thread.sleep(1000);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldProducer(), false);
            Thread.sleep(1000);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldConsumer(), false);
            thread(new HelloWorldProducer(), false);*/
        } catch (InterruptedException e) {
            System.out.println("Exp: " + e);
            e.printStackTrace();
        }
    }

    public static void thread(Runnable runnable, boolean daemon) {
        try {
            Thread brokerThread = new Thread(runnable);
            brokerThread.setDaemon(daemon);
            brokerThread.start();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(App.url);

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Create a messages
                String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
                TextMessage message = session.createTextMessage(text);

                // Tell the producer to send the message
                System.out.println("Sleeping producer for 10000");
                Thread.sleep(10000);

                producer.send(message);
                System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());

                // Clean up
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public void run() {
            try {

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(App.url);

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");

                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);

                // Wait for a message
                Message message = consumer.receive(1000);

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    System.out.println("Received: " + text);
                } else {
                    System.out.println("Received: " + message);
                }

                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }
}