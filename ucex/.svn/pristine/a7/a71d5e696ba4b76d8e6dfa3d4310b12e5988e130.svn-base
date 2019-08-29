package api.push;

import java.io.Serializable;

public final class GCMResult implements Serializable {

	private static final long serialVersionUID = -228532848063301197L;
	private final String messageId;
	private final String canonicalRegistrationId;
	private final String errorCode;

	static final class Builder {

		private String messageId;
		private String canonicalRegistrationId;
		private String errorCode;

		public Builder canonicalRegistrationId(String value) {
			canonicalRegistrationId = value;
			return this;
		}

		public Builder messageId(String value) {
			messageId = value;
			return this;
		}

		public Builder errorCode(String value) {
			errorCode = value;
			return this;
		}

		public GCMResult build() {
			return new GCMResult(this);
		}
	}

	private GCMResult(Builder builder) {
		canonicalRegistrationId = builder.canonicalRegistrationId;
		messageId = builder.messageId;
		errorCode = builder.errorCode;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getCanonicalRegistrationId() {
		return canonicalRegistrationId;
	}

	public String getErrorCodeName() {
		return errorCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		if (messageId != null) {
			builder.append(" messageId=").append(messageId);
		}
		if (canonicalRegistrationId != null) {
			builder.append(" canonicalRegistrationId=").append(canonicalRegistrationId);
		}
		if (errorCode != null) {
			builder.append(" errorCode=").append(errorCode);
		}
		return builder.append(" ]").toString();
	}

}
