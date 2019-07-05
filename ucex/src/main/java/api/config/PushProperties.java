package api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "push")
public class PushProperties {
	
	private int retry;
	private int timetolive;
	private int push_log_period;
	
	private String apnscert;
	private String apnspassword;
	private boolean apnsdev;
	
	private String androidpushserver;
	
	private String gcmkey;
	
	private String baidusecretkey;
	private String baiduapikey;
	
	
	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getApnscert() {
		return apnscert;
	}

	public void setApnscert(String apnscert) {
		this.apnscert = apnscert;
	}

	public String getApnspassword() {
		return apnspassword;
	}

	public void setApnspassword(String apnspassword) {
		this.apnspassword = apnspassword;
	}

	public boolean isApnsdev() {
		return apnsdev;
	}

	public void setApnsdev(boolean apnsdev) {
		this.apnsdev = apnsdev;
	}

	public String getGcmkey() {
		return gcmkey;
	}

	public void setGcmkey(String gcmkey) {
		this.gcmkey = gcmkey;
	}

	
	public int getPush_log_period() {
		return push_log_period;
	}

	public void setPush_log_period(int push_log_period) {
		this.push_log_period = push_log_period;
	}
	
	public int getTimetolive() {
		return timetolive;
	}

	public void setTimetolive(int timetolive) {
		this.timetolive = timetolive;
	}

	public String getAndroidpushserver() {
		return androidpushserver;
	}

	public void setAndroidpushserver(String androidpushserver) {
		this.androidpushserver = androidpushserver;
	}

	public String getBaidusecretkey() {
		return baidusecretkey;
	}

	public void setBaidusecretkey(String baidusecretkey) {
		this.baidusecretkey = baidusecretkey;
	}

	public String getBaiduapikey() {
		return baiduapikey;
	}

	public void setBaiduapikey(String baiduapikey) {
		this.baiduapikey = baiduapikey;
	}

}
