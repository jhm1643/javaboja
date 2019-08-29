package api.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ucex.test")
public class UcexTestProperties {

	private String mode;
	private List<String> serverlist = new ArrayList<String>();

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public List<String> getServerlist() {
		return serverlist;
	}

	public void setServerlist(String servers) {
		
		String[] serverList = servers.split(",");
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% setServerlist");
		for (String s : serverList) {
			serverlist.add(s);
			System.out.println(s);
		}
	}
	
}
