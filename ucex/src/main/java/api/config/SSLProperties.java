package api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ucex.ssl")
public class SSLProperties {
	private int port;

    private boolean clientAuth;
    private boolean enabled;
    private boolean secure;

    private String schema;
    private String protocol;
    private String keystoreAlias;
    private String keystorePassword;
    private String keystoreFile;
    private String keystoreType;
    
    private int http_port;
    
    private int apiServerSocketPort;
    
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isClientAuth() {
		return clientAuth;
	}
	public void setClientAuth(boolean clientAuth) {
		this.clientAuth = clientAuth;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getKeystoreAlias() {
		return keystoreAlias;
	}
	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}
	public String getKeystorePassword() {
		return keystorePassword;
	}
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	public String getKeystoreFile() {
		return keystoreFile;
	}
	public void setKeystoreFile(String keystoreFile) {
		this.keystoreFile = keystoreFile;
	}
	public String getKeystoreType() {
		return keystoreType;
	}
	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}
	public int getHttp_port() {
		return http_port;
	}
	public void setHttp_port(int http_port) {
		this.http_port = http_port;
	}
	public int getApiServerSocketPort() {
		return apiServerSocketPort;
	}
	public void setApiServerSocketPort(int apiServerSocketPort) {
		this.apiServerSocketPort = apiServerSocketPort;
	}
}
