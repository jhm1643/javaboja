package api.push;

public class PushObject {
	
	private String application;
	
	private String invokeid;
	private String userid;
	
	private String menu;
	private String title;
	private String from;
	private String fromtenant;
	private String mode;
	private String message;
	
	private String device;  // ios or android
	private String token;	// device token
	
	private String resultCode = "-1";
	private String resultMessage = "";
	
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getInvokeid() {
		return invokeid;
	}
	public void setInvokeid(String invokeid) {
		this.invokeid = invokeid;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getMenu() {
		return menu;
	}
	public void setMenu(String menu) {
		this.menu = menu;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getFromtenant() {
		return fromtenant;
	}
	public void setFromtenant(String fromtenant) {
		this.fromtenant = fromtenant;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultMessage() {
		return resultMessage;
	}
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	/*@Override
	public String toString() {
		return "PushObject [invokeid=" + invokeid + ", userid=" + userid
				+ ", menu=" + menu + ", title=" + title + ", from=" + from
				+ ", fromtenant=" + fromtenant + ", mode=" + mode
				+ ", message=" + message + ", device=" + device + ", token="
				+ token + ", resultCode=" + resultCode + ", resultMessage="
				+ resultMessage + "]";
	}*/
	@Override
	public String toString() {
		return "PushObject [invokeid=" + invokeid + ", userid=" + userid + ", menu=" + menu + ", title=" + title
				+ ", from=" + from + ", fromtenant=" + fromtenant + ", mode=" + mode + ", message=" + message
				+ ", device=" + device + ", token=" + token + ", resultCode=" + resultCode + ", resultMessage="
				+ resultMessage + "]";
	}
}
