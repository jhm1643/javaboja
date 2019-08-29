package api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import api.config.RabbitMQProperties;

@Component
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class Rabbit {

	@Autowired
	private RabbitMQProperties rabbitMQProperties;
	
	private static Logger logger = LoggerFactory.getLogger(Rabbit.class);

	private ObjectMapper mapper = new ObjectMapper();
	private UcexLogin loginInfo = null;

	private Connection connection = null;
	private Channel ch = null;
	private Connection connection_cube = null;
	private Channel ch_cube = null;

	public void init() throws Exception {
		logger.info("init RabbitMQ Sender Connection ...");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitMQProperties.getHost());
		factory.setUsername(rabbitMQProperties.getUserName());
		factory.setPassword(rabbitMQProperties.getUserPassword());
		connection = factory.newConnection();
		ch = connection.createChannel();
		ch.exchangeDeclare(rabbitMQProperties.getHistoryExchangeName(), "fanout");
		
		ConnectionFactory factory_cube = new ConnectionFactory();
		factory_cube.setHost(rabbitMQProperties.getHost());
		factory_cube.setUsername(rabbitMQProperties.getUserName());
		factory_cube.setPassword(rabbitMQProperties.getUserPassword());
		connection_cube = factory_cube.newConnection();
		ch_cube = connection_cube.createChannel();
		ch_cube.exchangeDeclare(rabbitMQProperties.getCubeExchangeName(), "fanout");
	}

	public UcexLogin getLoginInfo() {
		return loginInfo;
	}

	public void setLoginInfo(UcexLogin loginInfo) {
		this.loginInfo = loginInfo;
	}

	public void sendMessage(Object obj) throws Exception {
		sendMessage(mapper.writeValueAsString(obj));
	}

	public void sendMessage(String msg) throws Exception {
		
		if (ch == null || ch.isOpen() == false) {
			init();
		}
		
		logger.info("MESSAGE SEND : {}", msg);
		ch.basicPublish(rabbitMQProperties.getHistoryExchangeName(), "", null, msg.getBytes());
	}
	
	public void sendCubeMessage(String msg) throws Exception {

		if (ch_cube == null || ch_cube.isOpen() == false) {
			init();
		}
		
		msg = msg.replaceAll("null", "");
		
		logger.info("MESSAGE}(CUBE) SEND : {}", msg);
		ch_cube.basicPublish(rabbitMQProperties.getCubeExchangeName(), rabbitMQProperties.getCubeExchangeName(), null, msg.getBytes());
	}

	public void sendMessageCT(String cmd, String cm, String tenant) throws Exception {
		BusinessObject bo = new BusinessObject();
		bo.setCommand(cmd);
		bo.setCm(cm);
		bo.setTenant(tenant);
		sendMessage(bo);
	}
	
	public void sendMessageCTN(String cmd, String cm, String tenant, String number) throws Exception {
		BusinessObject bo = new BusinessObject();
		bo.setCommand(cmd);
		bo.setCm(cm);
		bo.setTenant(tenant);
		bo.setParam(number);
		sendMessage(bo);
	}
	
	public void sendMessageCTNO(String cmd, String cm, String tenant, String number, String obj) throws Exception {
		BusinessObject bo = new BusinessObject();
		bo.setCommand(cmd);
		bo.setCm(cm);
		bo.setTenant(tenant);
		bo.setParam(number);
		bo.setObj(obj);
		sendMessage(bo);
	}
	
	public void sendMessageCTO(String cmd, String cm, String tenant, String obj) throws Exception {
		BusinessObject bo = new BusinessObject();
		bo.setCommand(cmd);
		bo.setCm(cm);
		bo.setTenant(tenant);
		bo.setObj(obj);
		sendMessage(bo);
	}
	
	public void sendMessageO(String cmd, String obj) throws Exception {
		BusinessObject bo = new BusinessObject();
		bo.setCommand(cmd);
		bo.setObj(obj);
		sendMessage(bo);
	}
}
