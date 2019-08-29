package api.process;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillTask extends Thread {
	private static Logger	log = LoggerFactory.getLogger(KillTask.class);

	private ExecutorService execMain  = Executors.newSingleThreadExecutor();
	private EProcessState	curTaskState;
	// task main-job
	private	MyShellExecutor	taskMainJob = null;

	private	String			rootUserPassword = "";
	
	private String			taskId = "";
	private String			execCmd = "";

	//-----------------------------------------------------------------------------------------------------------------
	private KillTask() {
		//
	}

	public KillTask(String execCmd, String taskId) {
		this.execCmd = execCmd;

		if(taskId == null || taskId.isEmpty()) {
			taskId = "task_" + UUID.randomUUID().toString();
		}
		setTaskId(taskId);

		init();
	}

	private void init() {
		curTaskState = EProcessState.NONE;
		taskMainJob = new MyShellExecutor();
	}
	
	public String getTaskId() {
		return taskId;
	}
	
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public void run() {
		log.debug("STARTING.");

		final String[] toExecute = new String[] {
				"/bin/sh", "-c",
				execCmd + " -c ; " + "kill -9 `cat /tmp/" + taskId + "`"
			};

		taskMainJob.setShellExecutor(toExecute);

		curTaskState = EProcessState.STARTED;

		log.debug("START");

		execMain.execute(taskMainJob);

		log.debug("STARTED");

		execMain.shutdown();

		curTaskState = EProcessState.END;

		log.debug("END.");
	}

	public void shutdownNow() {
		log.info("Force shutdown.");
		// 강제 종료
		execMain.shutdownNow();
	}
	
	public MyShellExecutor getMainTask() {
		return taskMainJob;
	}

	public EProcessState getTaskState() {
		return curTaskState;
	}

	public String getRootUserPassword() {
		return rootUserPassword;
	}

	public void setRootUserPassword(String rootUserPassword) {
		this.rootUserPassword = rootUserPassword;
	}

}
