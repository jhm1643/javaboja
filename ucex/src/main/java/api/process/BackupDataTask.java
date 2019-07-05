package api.process;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import api.config.SystemBackupConfig;

public class BackupDataTask extends Thread implements IDataBackupTask {
	private static Logger	log = LoggerFactory.getLogger(BackupDataTask.class);

	public static String	RESULT_OUT_PATH = "/tmp";
	
	private SystemBackupConfig	systemBackupConfig = null;

	private ExecutorService execMain = Executors.newSingleThreadExecutor();
	private ExecutorService execWatch = Executors.newSingleThreadExecutor();
	
	private EProcessState	curTaskState;

	// task main-job
	private	MyShellExecutor	taskJob = null;
	// task result-output-file
	private	WatchResult		taskWatchResult = null;
	
	private String			backupTaskId = "";

	//-----------------------------------------------------------------------------------------------------------------
	public static BackupDataTask getTask(ServletContext servletContext, String taskId) {
		return (BackupDataTask) servletContext.getAttribute(taskId);
	}

	public static void saveTask(ServletContext servletContext, String taskId, BackupDataTask dataBackupTask) {
		servletContext.setAttribute(taskId, dataBackupTask);
	}

	public static void removeTask(ServletContext servletContext, String taskId) {
		servletContext.removeAttribute(taskId);
	}

	//-----------------------------------------------------------------------------------------------------------------
	public BackupDataTask(SystemBackupConfig systemBackupConfig) {
		this.systemBackupConfig = systemBackupConfig;
		initTask();
	}
	
	private void initTask() {
		curTaskState = EProcessState.NONE;

		//taskWatchResult = new WatchResult(ConfigUtil.RESULT_OUT_PATH, ConfigUtil.BACKUP_RESULT_FILE);
		taskJob = new MyShellExecutor();
	}

	public String getTaskId() {
		return backupTaskId;
	}
	
	public void setTaskId(String backupTaskId) {
		if(backupTaskId == null || backupTaskId.isEmpty()) {
			this.backupTaskId = "task_" + UUID.randomUUID().toString();
		} else {
			this.backupTaskId = backupTaskId;
		}
	}

	/*
	 * String[] commands
	 * 
	 * 		{ "/bin/sh", "-c", "/home/cmuc/bin/sh.cloud_backup 'backup-path' 'backup-id'" }
	 * 
	 */

	@Override
	public void run() {
		curTaskState = EProcessState.STARTING;
		log.debug("STARTING. backup_id={}", backupTaskId);

		try {
			taskWatchResult = new WatchResult(RESULT_OUT_PATH, backupTaskId, null);
			log.debug("STARTING. Set Watch Task.");

			String	backupCommand = systemBackupConfig.getCmd();
			String	backupPath = systemBackupConfig.getPath();
			
			log.info("BACKUP cmd={}, path={}, id={}", backupCommand, backupPath, backupTaskId);

			final String[] toExecute = new String[] {
					"/bin/sh", "-c",
					backupCommand + " '" + backupPath + "' '" + backupTaskId + "'"
				};
	
			taskJob.setShellExecutor(toExecute);
	
			curTaskState = EProcessState.STARTED;
			log.debug("STARTED");
	
			execWatch.execute(taskWatchResult);
			execMain.execute(taskJob);
	
			log.debug("STARTED");
	
			execMain.shutdown();
			execWatch.shutdown();

			curTaskState = EProcessState.END;
			log.debug("END.");
		} catch(Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();

			curTaskState = EProcessState.FAILURE;
			log.debug("FAILURE.");
		}
	}

	public void shutdownNow() {
		log.info("Force shutdown.");
		// 강제 종료
		execMain.shutdownNow();
		execWatch.shutdownNow();

		curTaskState = EProcessState.END;
		log.debug("END.");
	}
	
	public MyShellExecutor getJobTask() {
		return taskJob;
	}
	
	public WatchResult getWatchTask() {
		return taskWatchResult;
	}

	public EProcessState getTaskState() {
		return curTaskState;
	}

	public boolean isEnded() {
		log.debug("isEnded STATUS backupTask={}, backupJob={}", curTaskState, taskJob.isEnded());

		if(curTaskState == EProcessState.NONE) return true;
		if(curTaskState == EProcessState.FAILURE) return true;
		if(curTaskState == EProcessState.END && taskJob.isEnded()) return true;
		return false;
	}

	public boolean isError() {
		log.debug("isError STATUS backupTask={}, backupJob={}", curTaskState, taskJob.isError());

		if(curTaskState == EProcessState.FAILURE) return true;
		if(curTaskState == EProcessState.STARTED && taskJob.isError()) return true;
		return false;
	}

	public boolean isRunning() {
		log.debug("isRunning STATUS backupTask={}, backupJob={}", curTaskState, taskJob.isRunning());

		if(curTaskState == EProcessState.STARTING) return true;
		if(curTaskState == EProcessState.STARTED) return true;
		if(curTaskState == EProcessState.END && taskJob.isRunning()) return true;
		return false;
	}

	@Override
	public void updateWatchFile(Properties props) {
		//
	}

	@Override
	public boolean getContinueFlag() {
		return false;
	}

}
