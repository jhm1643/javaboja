package api.process;

import java.util.Properties;

public interface IDataBackupTask {
	public static final int CTRL_NONE = 0;

	public static final int CTRL_START_OK = 200;	// Succes
	public static final int CTRL_STARTED = 300;		// Backup Running
	public static final int CTRL_START_INTERNAL_ERROR = 500;	// Internal Error

	public static final int CTRL_STATE_OK = 200;		// 처리 완료(종료)
	public static final int CTRL_STATE_RUNNING = 202;	// Backup Running
	public static final int CTRL_STATE_INTERNAL_ERROR = 500;	// Internal Error

	public static final int CTRL_STOP_OK = 200;		// Succes
	public static final int CTRL_STOPPED = 300;		// Backup 이미 종료
	public static final int CTRL_STOP_INTERNAL_ERROR = 500;	// Internal Error

	public void updateWatchFile(Properties props);
	public boolean getContinueFlag();

}
