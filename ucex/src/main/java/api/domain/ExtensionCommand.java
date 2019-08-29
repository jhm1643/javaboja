package api.domain;

public class ExtensionCommand {

	private String tenant;
	private String number;
	private ExtensionData data;
	
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public ExtensionData getData() {
		return data;
	}
	public void setData(ExtensionData data) {
		this.data = data;
	}
		
}
