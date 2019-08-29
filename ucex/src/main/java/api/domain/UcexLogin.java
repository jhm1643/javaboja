package api.domain;

import java.util.List;

public class UcexLogin {

	private String login_password;
	private String client_ip;
	private String timestamp;
	
	private String hash;
	private String poll_interval;
	private List<String> server_list;
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getPoll_interval() {
		return poll_interval;
	}
	public void setPoll_interval(String poll_interval) {
		this.poll_interval = poll_interval;
	}
	public List<String> getServer_list() {
		return server_list;
	}
	public void setServer_list(List<String> server_list) {
		this.server_list = server_list;
	}
	public String getLogin_password() {
		return login_password;
	}
	public void setLogin_password(String login_password) {
		this.login_password = login_password;
	}
	public String getClient_ip() {
		return client_ip;
	}
	public void setClient_ip(String client_ip) {
		this.client_ip = client_ip;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "UcexLogin [login_password=" + login_password + ", client_ip=" + client_ip + ", timestamp=" + timestamp
				+ ", hash=" + hash + ", poll_interval=" + poll_interval + ", server_list=" + server_list + "]";
	}

}
