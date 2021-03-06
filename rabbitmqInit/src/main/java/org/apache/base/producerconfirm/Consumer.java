package org.apache.base.producerconfirm;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *@author Mark老师   享学课堂 https://enjoy.ke.qq.com
 *往期视频咨询芊芊老师  QQ：2130753077  VIP课程咨询 依娜老师  QQ：2470523467
 *类说明：
 */
public class Consumer {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        //1、创建工厂，设置rabbitmq服务器地址
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setVirtualHost("test_rabbitmq");
        factory.setUsername("admin");
        factory.setPassword("admin");
        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

//        channel.exchangeDeclare(ProducerConfirm.EXCHANGE_NAME,BuiltinExchangeType.DIRECT);
//        channel.exchangeDeclare(ProducerBatchConfirm.EXCHANGE_NAME,BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(ProducerConfirmAsync.EXCHANGE_NAME,BuiltinExchangeType.DIRECT);

//        String queueName = ProducerConfirm.EXCHANGE_NAME;
//        String queueName = ProducerBatchConfirm.EXCHANGE_NAME;
        String queueName = ProducerConfirmAsync.EXCHANGE_NAME;
        channel.queueDeclare(queueName,false,false,
                false,null);

        //只关注error级别的日志
        String severity="error";
//        channel.queueBind(queueName, ProducerConfirm.EXCHANGE_NAME,severity);
//        channel.queueBind(queueName, ProducerBatchConfirm.EXCHANGE_NAME,severity);
        channel.queueBind(queueName, ProducerConfirmAsync.EXCHANGE_NAME,severity);

        System.out.println(" [*] Waiting for messages......");

        // 创建队列消费者
        final com.rabbitmq.client.Consumer consumerB = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //记录日志到文件：
                System.out.println( "Received ["+ envelope.getRoutingKey()
                        + "] "+message);
            }
        };
        channel.basicConsume(queueName, true, consumerB);
    }

}
