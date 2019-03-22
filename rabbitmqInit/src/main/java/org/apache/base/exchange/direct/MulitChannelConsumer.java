package org.apache.base.exchange.direct;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：一个连接多个信道；一个线程绑定一个信道
 */
public class MulitChannelConsumer {

    private static class ConsumerWorker implements Runnable{
        final Connection connection;

        public ConsumerWorker(Connection connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                //3、创建信道
                final Channel channel = connection.createChannel();
                channel.exchangeDeclare(DirectProducer.EXCHANGE_NAME,
                        "direct");
                //4、如果声明每个信道绑定一个随机队列 则多个信道收回收到同一条消息，如果每个信道绑定固定的队列名，则轮询消费队列上的消息
                String queueName = "focusAll1"; //固定的队列名（如果没有收到消息，愿意是旧队列影响到了，则修改一个队列名）
                /*声明一个队列,rabbitmq，如果队列已存在，不会重复创建*/
                channel.queueDeclare(queueName,false, false,false, null);
//                String queueName = channel.queueDeclare().getQueue();//随机的队列名
                //消费者名字，打印输出用
                final String consumerName =  Thread.currentThread().getName()+"-all";
                //所有日志严重性级别
                String[] severities={"error","info","warning"};
                for (String severity : severities) {
                    //5、路由键绑定队列
                    channel.queueBind(queueName, DirectProducer.EXCHANGE_NAME, severity);
                }
                // 6、创建队列消费者
                final Consumer consumerA = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(consumerName + " Received " + envelope.getRoutingKey() + ":'" + message + "'");
                    }
                };
                //7、开始接受消息（异步方法）
                channel.basicConsume(queueName, true, consumerA);
                System.out.println("["+consumerName+"] Waiting for messages:");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] argv) throws IOException,
            InterruptedException, TimeoutException {
        //1、创建工厂，设置rabbitmq服务器地址
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setVirtualHost("test_rabbitmq");
        factory.setUsername("admin");
        factory.setPassword("admin");

        //2、打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        //一个连接多个信道
        for(int i=0;i<2;i++){
            /*将连接作为参数，传递给每个线程*/
            Thread worker =new Thread(new ConsumerWorker(connection));
            worker.start();
        }
    }
}
