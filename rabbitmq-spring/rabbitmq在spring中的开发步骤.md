修改applicationContext.xml
1、配置连接工厂
	<!-- rabbitMQ配置 -->
	<bean id="rabbitConnectionFactory"
		  class="org.springframework.amqp.rabbit.connection.CachingConnectionFactory">
		<constructor-arg value="127.0.0.1"/>
		<property name="username" value="guest"/>
		<property name="password" value="guest"/>
		<property name="channelCacheSize" value="8"/>
		<property name="port" value="5672"></property>
	</bean>
	<!--Spring的rabbitmq admin-->
	<rabbit:admin connection-factory="rabbitConnectionFactory"/>
	
2、配置模板bean
    <!-- 创建rabbitTemplate 消息模板类 -->
    <bean id="rabbitTemplate" class="org.springframework.amqp.rabbit.core.RabbitTemplate">
        <constructor-arg ref="rabbitConnectionFactory"/>
    </bean> 
ps：或者使用<rabbit:template id="rabbitTemplate" connection-factory="rabbitConnectionFactory" >

3、配置队列：
    <rabbit:queue name="h4_queue" durable="false">durable的值根据控制台上查询h4_queue队列的属性来填写，值不对的话会报错
      </rabbit:queue>
      
4、配置交换器
  	<!--fanout交换器；交换器类型有4种-->
      <rabbit:fanout-exchange name="fanout-exchange"
            xmlns="http://www.springframework.org/schema/rabbit" durable="false">
          <bindings>
              <binding queue="h4_queue"></binding> 绑定队列
          </bindings>
      </rabbit:fanout-exchange>
      
ps：开发过程中，消费者和生产者都配置上队列和交换器，避免生产者在发送消息时，rabbitmq服务上没有想应的队列导致消息丢失了。

5、使用模板对象发送消息；具体参考cn.enjoyedu.controller.RabbitMqController
注入
    @Autowired
    RabbitTemplate rabbitTemplate;

rabbitTemplate.send("fanout-exchange","",
                        new Message(str.getBytes(),new MessageProperties()));
或者：
rabbitTemplate.send("topic_exchange",
                            routeKey,
                            new Message(str.getBytes(), messageProperties));