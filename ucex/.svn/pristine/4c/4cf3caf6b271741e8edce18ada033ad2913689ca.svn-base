package api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import api.config.RabbitMQProperties;
import api.push.PushObject;
import api.push.PushService;

@Component
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class QueueReceiver {
	
	@Autowired
	private RabbitMQProperties rabbitMQProperties;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutator;
	
	@Autowired
	private ApplicationContext ctx;
	
//	@Autowired
//	private BusinessWorker bw;

	private static Logger logger = LoggerFactory.getLogger(QueueReceiver.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	private Connection connection = null;
	private Channel ch = null;
	private QueueingConsumer consumer = null;
 
	public void init() throws Exception {
/*	/2019.01.15 martino Push Service는 ucex에서 실행하지 않음	/			
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true );
		
		logger.info("init RabbitMQ Receiver Connection ...");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitMQProperties.getHost());
		factory.setUsername(rabbitMQProperties.getUserName());
		factory.setPassword(rabbitMQProperties.getUserPassword());
		connection = factory.newConnection();
		ch = connection.createChannel();
		ch.exchangeDeclare(rabbitMQProperties.getExchangeName(), "fanout");

		ch.queueBind(rabbitMQProperties.getQueueName(), rabbitMQProperties.getExchangeName(), "");

		consumer = new QueueingConsumer(ch);
		ch.basicConsume(rabbitMQProperties.getQueueName(), true, consumer);	
*/
	}

	public void receiveMessage() throws Exception {
/*		//2019.01.15 martino Push Service는 ucex에서 실행하지 않음			

//		 test code
//		 if (true) return;

		if (consumer == null || ch == null || ch.isOpen() == false) {
			init();
		}

		while (true) {

			QueueingConsumer.Delivery delivery = consumer.nextDelivery(rabbitMQProperties.getReadTimeout());

			if (delivery == null) {
//				continue;
				return;
			}

			String message = new String(delivery.getBody());
			logger.info("Received <{}>", message);
			
//			BusinessObject bo = mapper.readValue(message, BusinessObject.class);
//			bw.run(bo, bo.getObj());
			
			try {
				PushObject po = mapper.readValue(message, PushObject.class);

				PushService ps = (PushService) ctx.getBean("pushService");
				ps.setPo(po);
				
				taskExecutator.execute( ps );

			} catch (Exception e) {
				logger.error(e.toString(), e);
				continue;
			}
		}
*/
	}
}
