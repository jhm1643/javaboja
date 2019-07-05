package api.domain;

public class BusinessObject {

	private String command;
	private String cm;
	private String tenant;
	private String param;
	private String obj;
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getCm() {
		return cm;
	}
	public void setCm(String cm) {
		this.cm = cm;
	}
	public String getObj() {
		return obj;
	}
	public void setObj(String obj) {
		this.obj = obj;
	}
	
}
