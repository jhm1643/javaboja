package api.domain.backup;

public class SystemBackup {
	private String backup_id = "";

	public String getBackup_id() {
		return backup_id;
	}

	public void setBackup_id(String backup_id) {
		this.backup_id = backup_id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemBackup [backup_id=");
		builder.append(backup_id);
		builder.append("]");
		return builder.toString();
	}

}
