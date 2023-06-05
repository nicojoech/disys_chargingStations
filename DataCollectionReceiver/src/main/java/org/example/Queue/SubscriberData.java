package org.example.Queue;

import com.rabbitmq.client.*;
import org.example.Services.DataCollectionService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SubscriberData {
    private final static String queueName = "dataGatheringQueue";

    public static void receive(int number, DataCollectionService dataCollectionService) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(30003);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            private int messageCount = 0;

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {

                String message = new String(body, "UTF-8");
                dataCollectionService.writeMsgToList(message);
                System.out.println("Received message: " + message);
                messageCount++;

                if (messageCount == number) {
                    dataCollectionService.formatAndPublish();
                    messageCount = 0;
                }
            }
        };

        channel.basicConsume(queueName, true, consumer);


    }
}
