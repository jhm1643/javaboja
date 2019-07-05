package api.domain_acd;

public class AcdSupervisorNumberCommand {
	private String tenant;
	private String group_id;
	private AcdSupervisorData data;
	
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getGroup_id() {
		return group_id;
	}
	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}
	public AcdSupervisorData getData() {
		return data;
	}
	public void setData(AcdSupervisorData data) {
		this.data = data;
	}
}
