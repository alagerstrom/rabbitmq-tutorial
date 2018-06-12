import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class EmitLog {
    private static final String EXCHANGE_NAME = "logs";
    private static final String HOST = "localhost";

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(HOST);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        Util.promptLoop("Enter message", input -> {
            try {
                channel.basicPublish(EXCHANGE_NAME, "", null, input.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(" [x] Sent: " + input);
        });

        channel.close();
        connection.close();
    }
}
