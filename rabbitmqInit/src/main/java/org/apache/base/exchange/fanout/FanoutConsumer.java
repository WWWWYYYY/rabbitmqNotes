package org.apache.base.exchange.fanout;

import com.rabbitmq.client.*;
import org.apache.base.exchange.direct.DirectProducer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class FanoutConsumer {

    /**
     * 1、创建连接工厂
     * 2、通过连接工厂创建连接
     * 3、通过连接创建信道
     * 4、在信道上声明一个交换器
     * 5、在信道上声明一个队列并在把信道、队列、交换器、路由键绑定在一起
     * 6、定义消费者，消费者的消息处理
     * 7、绑定队列名和消费者后等待消息
     *
     * rabbitmq在生产消息或者消费消息都必须建立在信道基础上；一个连接可以建立多个信道，一个信道可以接收一个或者多个队列，一个队列可以绑定一个或者多个路由键。
     * 一个消息可以被多个队列同时消费。同一个队列的消息可以被多个信道上接收该队列的消费者轮询消费
     *
     * exchange类型为fanout时则类似广播模式，每个队列都会受到消息，不论队列绑定了什么样的路由键
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        //1、创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setVirtualHost("test_rabbitmq");
        factory.setUsername("admin");
        factory.setPassword("admin");
        //2、通过连接工厂创建连接
        Connection connection = factory.newConnection();
        //3、通过连接创建信道
        Channel channel = connection.createChannel();
        //4、在信道上声明一个交换器
        channel.exchangeDeclare(FanoutProducer.EXCHANGE_NAME,BuiltinExchangeType.FANOUT);//
        //5、在信道上声明一个队列并在把信道、队列、交换器、路由键绑定在一起
        String queueName = "fanout_queue";
        String queueName2 = "fanout_queue2";
        channel.queueDeclare(queueName,false,false,false,null);
        channel.queueDeclare(queueName2,false,false,false,null);
        //一个信道channel可以绑定一个或多个队列；一个信道可以可以为一个队列绑定多个路由键；
        channel.queueBind(queueName,FanoutProducer.EXCHANGE_NAME,"error");
//        channel.queueBind(queueName,MandatoryProducer.EXCHANGE_NAME,"info");
//        channel.queueBind(queueName,MandatoryProducer.EXCHANGE_NAME,"warning");
        channel.queueBind(queueName2,FanoutProducer.EXCHANGE_NAME,"error");
        System.out.println("waiting for message........");

        //6、定义消费者，消费者的消息处理
        final Consumer consumer = new DefaultConsumer(channel){
            //消息处理
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received["+envelope.getRoutingKey()+"]"+message);
            }
        };
        //7、绑定队列名和消费者后等待消息
        channel.basicConsume(queueName,true,consumer);//异步方法
        channel.basicConsume(queueName2,true,consumer);//异步方法
        System.out.println(123);
    }
}
