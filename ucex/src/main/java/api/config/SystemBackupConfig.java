package api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "cloud.backup")
public class SystemBackupConfig {
	private String path;
	private String cmd;

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getCmd() {
		return cmd;
	}
	public void setcmd(String cmd) {
		this.cmd = cmd;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemBackupConfig [path=");
		builder.append(path);
		builder.append(", cmd=");
		builder.append(cmd);
		builder.append("]");
		return builder.toString();
	}

}
