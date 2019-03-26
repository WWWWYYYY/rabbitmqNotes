package org.apache.base.mandatory;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MandatoryProducer {

    public final static String EXCHANGE_NAME = "mandatory_test";
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
     */
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
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
        channel.exchangeDeclare(EXCHANGE_NAME,BuiltinExchangeType.DIRECT);

        //返回的失败通知;对于消息没有从交换器放到队列上时执行；但是由于网络原因回调函数可能不能正常调用，因此不能通过此方式保证消息的可靠性
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body);
                System.out.println("返回的replyText ："+replyText);
                System.out.println("返回的exchange ："+exchange);
                System.out.println("返回的routingKey ："+routingKey);
                System.out.println("返回的message ："+msg);
            }
        });

        //连接关闭时执行 ;处理资源释放的工作
        connection.addShutdownListener(new ShutdownListener() {
            public void shutdownCompleted(ShutdownSignalException cause) {
                System.out.println(cause.getMessage());
            }
        });

        //信道关闭时执行 ；处理资源释放的工作
        channel.addShutdownListener(new ShutdownListener() {
            public void shutdownCompleted(ShutdownSignalException cause) {
                System.out.println(cause.getMessage());
            }
        });

        //5、定义路由键;这里定义了3个路由键
        String[] routekeys = {"error","info","warning"};
        for(int i=0;i<3;i++){
            String routekey = routekeys[i%3];
            String msg = "Hellol,RabbitMq"+(i+1);
            //6、发送消息;发布消息参数：交换器名称，路由键，基础参数，消息byte数组
            channel.basicPublish(EXCHANGE_NAME,routekey,true,null,
                    msg.getBytes());
            System.out.println("Sent [routekey="+routekey+"] msg:"+msg);
            Thread.sleep(200);
        }


        channel.close();
        connection.close();
    }
}
