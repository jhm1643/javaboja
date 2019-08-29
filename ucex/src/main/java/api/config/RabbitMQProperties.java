package api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ucex.q")
public class RabbitMQProperties {

	private String exchangeName;
	private String cubeExchangeName;
	private String historyExchangeName;
	private String queueName;
	private String host;
	private String userName;
	private String userPassword;
	private int	readTimeout;
	
	public String getExchangeName() {
		return exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	public String getCubeExchangeName() {
		return cubeExchangeName;
	}
	public void setCubeExchangeName(String cubeExchangeName) {
		this.cubeExchangeName = cubeExchangeName;
	}
	public String getHistoryExchangeName() {
		return historyExchangeName;
	}
	public void setHistoryExchangeName(String historyExchangeName) {
		this.historyExchangeName = historyExchangeName;
	}
	
}
