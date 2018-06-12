import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class RPCClient {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        String input = "something";
        RPCClient client = new RPCClient();
        while (!input.equals("quit")){
            input = Util.prompt("Calculate fibonacci: (or type quit)");
            final String message = input;
            CompletableFuture.runAsync(
                    () -> client.call(message, new CompletionHandler<String, Void>() {
                        @Override
                        public void completed(String result, Void attachment) {
                            System.out.println(" [x] fib(" + message +") = " + result);
                        }

                        @Override
                        public void failed(Throwable exc, Void attachment) {
                            System.out.println(" [!] error when calculating fib(" + message +"): " + exc.getMessage());
                        }
                    }));
        }
        client.close();
    }

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

    }

    public void call(String message, CompletionHandler<String, Void> completionHandler) {

        try {
            replyQueueName = channel.queueDeclare().getQueue();
            final String corrId = UUID.randomUUID().toString();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

            channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });

            String result = response.take();
            completionHandler.completed(result, null);

        }catch (Exception e){
            completionHandler.failed(e, null);
        }
    }

    public void close() throws IOException {
        connection.close();
    }

}