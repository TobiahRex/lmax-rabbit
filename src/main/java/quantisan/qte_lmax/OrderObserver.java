package quantisan.qte_lmax;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OrderObserver implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(OrderObserver.class);
    private final Channel channel;
    private final QueueingConsumer consumer;

    public OrderObserver(Channel channel, QueueingConsumer consumer) {
        this.channel = channel;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            QueueingConsumer.Delivery delivery = null;
            try {
                delivery = consumer.nextDelivery();
            } catch (InterruptedException e) {
                logger.error("Interrupted before order delivery.", e);
            }
            Order order = new Order(new String(delivery.getBody()));
            order.execute();
            logger.info("Received order '{}'", order);
            try {
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (IOException e) {
                logger.error("Fail to acknowledge order message.", e);
            }
        }
    }
}
