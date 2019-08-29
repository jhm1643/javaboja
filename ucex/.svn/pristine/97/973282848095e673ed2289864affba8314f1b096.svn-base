package api.process;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyShellExecutor extends AbstractProcess implements Runnable {
	private static Logger	log = LoggerFactory.getLogger(MyShellExecutor.class);

	/**
	 * stores the command to be executed
	 * 
	 * new String[]{ "/bin/sh", "-c", "CMD" };
	 */
	private String[]	commands;

	//-----------------------------------------------------------------------------------------------------------------
	public MyShellExecutor() {
		//
	}

	public MyShellExecutor(final String[] commands) {
		setShellExecutor(commands);
	}
	
	public String[] getShellExecutor() {
		return commands;
	}

	public void setShellExecutor(final String[] commands) {
		this.commands = commands;
	}

	//-----------------------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		initialize();

		setProcessState(EProcessState.STARTING);

		StringBuilder	sb = new StringBuilder();
		for(int i = 0; i < commands.length; i++) {
			if(sb.length() > 0) {
				sb.append(",");
			}
			sb.append(commands[i]);
		}

		String	cmds = sb.toString();
		log.debug("Command=[" + cmds + "]");

		// Empty or ',' X length
		if(cmds.isEmpty() || cmds.length() == commands.length) {
			setProcessState(EProcessState.FAILURE);
			log.error("Command invalid.");
			return;
		}

		try {
			setProcessState(EProcessState.STARTED);

			startProcess(commands);

			log.info("pid=" + getPid());

			//Wait for the command to complete, and check if the exit value was 0 (success)
			int	waitForValue = waitFor();
			int	exitValue = getExitValue();

			log.debug("waitForValue=" + waitForValue);

			if(waitForValue == 0) {
				//normally terminated, a way to read the output
				log.debug("CMD SUCCESS. exitValue=" + exitValue + ", cmd=" + getCommands());

				if(exitValue == 0) {
					setProcessState(EProcessState.END);
				} else {
					setProcessState(EProcessState.FAILURE);
				}
			} else {
				setProcessState(EProcessState.FAILURE);

				// abnormally terminated, there was some problem
				//a way to read the error during the execution of the command
				log.debug("CMD FAILURE. exitValue=" + exitValue + ", cmd=" + getCommands());
			}

			log.info("RESULT state=" + getProcessState());

			log.info("stdMsg=", getStdOutMsg());
			log.info("errMsg=", getErrOutMsg());
		} catch (IOException e) {
			setProcessState(EProcessState.FAILURE);
			e.printStackTrace();
			log.error(e.getMessage());
		} finally {
			//destroy();
		}
	}

	public boolean isEnded() {
		if(getProcessState() == EProcessState.NONE) return true;
		if(getProcessState() == EProcessState.FAILURE) return true;
		if(getProcessState() == EProcessState.END) return true;
		return false;
	}

	public boolean isError() {
		if(getProcessState() == EProcessState.FAILURE) return true;
		return false;
	}

	public boolean isRunning() {
		if(getProcessState() == EProcessState.STARTING) return true;
		if(getProcessState() == EProcessState.STARTED) return true;
		return false;
	}

}
