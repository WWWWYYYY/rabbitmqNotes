package org.apache.base.exchange.fanout;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class FanoutProducer {

    public final static String EXCHANGE_NAME = "fanout_logs";
    /**
     * 1、创建工厂，设置rabbitmq服务器地址
     * 2、通过工厂创建连接
     * 3、通过连接创建信道（一个连接下有多个信道）
     * 4、通过信道创建交换器 ，设置交换器的名称，后续发送消息通过交换器名称找到对应的交换器（一个信道可以申明多个交换器吗？）
     * 5、定义路由键
     * 6、发送消息
     *
     * rabbitmq在生产消息或者消费消息都必须建立在信道基础上；一个连接可以建立多个信道，一个信道可以接收一个或者多个队列，一个队列可以绑定一个或者多个路由键。
     * 一个消息可以被多个队列同时消费。同一个队列的消息可以被多个信道上接收该队列的消费者轮询消费
     *
     * exchange类型为fanout时则类似广播模式，每个队列都会受到消息，不论队列绑定了什么样的路由键
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        //1、创建工厂，设置rabbitmq服务器地址
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setVirtualHost("test_rabbitmq");
        factory.setUsername("admin");
        factory.setPassword("admin");
        //2、通过工厂创建连接
        Connection connection = factory.newConnection();
        //3、通过连接创建信道（一个连接下有多个信道）
        Channel channel = connection.createChannel();
        //4、通过信道创建交换器
        channel.exchangeDeclare(EXCHANGE_NAME,BuiltinExchangeType.FANOUT);
        //5、定义路由键;这里定义了3个路由键
        String[] routekeys = {"error1","info1","warning1"};
        for(int i=0;i<3;i++){
            String routekey = routekeys[i%3];
            String msg = "Hellol,RabbitMq"+(i+1);
            //6、发送消息;发布消息参数：交换器名称，路由键，基础参数，消息byte数组
            channel.basicPublish(EXCHANGE_NAME,routekey,null,
                    msg.getBytes());
            System.out.println("Sent [routekey="+routekey+"] msg:"+msg);
        }

      /*  如果重要的信息可以在生产者方创建队列
        String queueName = "focuserror";
        channel.queueDeclare(queueName,false,false,false,null);
        String routekey = "error";*//*表示只关注error级别的日志消息*//*
        channel.queueBind(queueName,MandatoryProducer.EXCHANGE_NAME,routekey);*/

        channel.close();
        connection.close();
    }
}
