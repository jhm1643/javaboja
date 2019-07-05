package api.domain.backup;

public class SystemBackupResponse {
	private int code = 0;
	private String desc = "";

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemBackupResponse [code=");
		builder.append(code);
		builder.append(", desc=");
		builder.append(desc);
		builder.append("]");
		return builder.toString();
	}

	public String toJsonString() {
		StringBuilder builder = new StringBuilder();
		/*
		builder.append("{code='");
		builder.append(code);
		builder.append("', desc='");
		builder.append(desc);
		builder.append("']");
		*/
		builder.append("{\"code\":\"");
		builder.append(code);
		builder.append("\"}");
		
		return builder.toString();
	}

}
