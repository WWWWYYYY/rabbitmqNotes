package org.apache.base.exchange.topic;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *@author Mark老师   享学课堂 https://enjoy.ke.qq.com
 *往期视频咨询芊芊老师  QQ：2130753077  VIP课程咨询 依娜老师  QQ：2470523467
 *类说明：
 */
public class TopicConsumer {

    public static void main(String[] argv) throws IOException,
            InterruptedException, TimeoutException {
        //1、创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setVirtualHost("test_rabbitmq");
        factory.setUsername("admin");
        factory.setPassword("admin");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.exchangeDeclare(TopicProducer.EXCHANGE_NAME,
                BuiltinExchangeType.TOPIC);
        // 声明一个随机队列
        String queueName = channel.queueDeclare().getQueue();
//根据业务设计不同的路由键
//        channel.queueBind(queueName,TopicProducer.EXCHANGE_NAME,"#"); //接收所有消息
//        channel.queueBind(queueName,TopicProducer.EXCHANGE_NAME,"*.email.*"); //接收任意服务器上的email
//        channel.queueBind(queueName,TopicProducer.EXCHANGE_NAME,"error.#");   //接收错误级别的消息
//        channel.queueBind(queueName,TopicProducer.EXCHANGE_NAME,"error.*.A"); //接收A服务器上错误界别的消息
        channel.queueBind(queueName,TopicProducer.EXCHANGE_NAME,"#.B");   //接收B服务器上的消息

        System.out.println(" [*] Waiting for messages:");

        // 创建队列消费者
        final Consumer consumerA = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" AllConsumer Received "
                        + envelope.getRoutingKey()
                        + "':'" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumerA);
    }
}
