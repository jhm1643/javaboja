package api.domain;

import java.util.List;

public class MadnMemberRequest {

	private List<ExtensionCommand> data;

	public List<ExtensionCommand> getData() {
		return data;
	}

	public void setData(List<ExtensionCommand> data) {
		this.data = data;
	}
}
