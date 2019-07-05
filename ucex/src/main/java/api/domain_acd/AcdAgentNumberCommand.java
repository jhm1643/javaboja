package api.domain_acd;


public class AcdAgentNumberCommand {
	private String tenant;
	private String group_id;
	private AcdAgentData data;
	
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public AcdAgentData getData() {
		return data;
	}
	public void setData(AcdAgentData data) {
		this.data = data;
	}
	public String getGroup_id() {
		return group_id;
	}
	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}
	
}
