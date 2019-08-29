package api.process;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractProcess /* implements IDataBackupTask */ {
	private static Logger	log = LoggerFactory.getLogger(AbstractProcess.class);

	private EProcessState	processState;

	private Process			runtimeProcess = null;

	private List<String>	commands = null;
	private long			runtimePid = -1L;
	private int				runtimeExitValue = -1;
	
	private ProcessStreamReader	thrStdOut = null;
	private ProcessStreamReader	thrErrOut = null;

	//-----------------------------------------------------------------------------------------------------------------
	public AbstractProcess() {
		initialize();
	}

	public void initialize() {
		processState = EProcessState.NONE;
		runtimePid = -1L;
		runtimeExitValue = -1;
	}

	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	public void destroy() {
		if(thrStdOut != null) {
			thrStdOut.interrupt();
			thrStdOut = null;
		}

		if(thrErrOut != null) {
			thrErrOut.interrupt();
			thrErrOut = null;
		}

		if(runtimeProcess != null) {
			runtimeProcess.destroy();
			runtimeProcess = null;
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	synchronized public EProcessState getProcessState() {
		return processState;
	}

	synchronized public void setProcessState(EProcessState processState) {
		this.processState = processState;
	}

	public int getExitValue() {
		return runtimeExitValue;
	}

	public long getPid() {
		return runtimePid;
	}
	
	public List<String> getCommands() {
		return commands;
	}

	public String getStdOutMsg() {
		if(thrStdOut == null || thrStdOut.getBuffer() == null) {
			return "";
		}
		return thrStdOut.getBuffer().toString();
	}
	
	public String getErrOutMsg() {
		if(thrErrOut == null || thrErrOut.getBuffer() == null) {
			return "";
		}
		return thrErrOut.getBuffer().toString();
	}

	//-----------------------------------------------------------------------------------------------------------------
	public Process startProcess(final String[] command) throws IOException {
		ProcessBuilder	builder = new ProcessBuilder(command);

		runtimeProcess = builder.start();
		commands = builder.command();

		startOutputGethering();
		runtimePid = Utils.getPidOfProcess(runtimeProcess);
		
		return runtimeProcess;
	}

	private void startOutputGethering() {
		if(runtimeProcess != null) {
			thrStdOut = new ProcessStreamReader(runtimeProcess.getInputStream(), false);
			thrStdOut.start();

			thrErrOut = new ProcessStreamReader(runtimeProcess.getErrorStream(), true);
			thrErrOut.start();
		}
	}

	public int waitFor() {
		int	retWaitFor = -1;
		
		try {
			retWaitFor = runtimeProcess.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		}
		
		try {
			runtimeExitValue = runtimeProcess.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		}

		return retWaitFor;
	}

//	@Override
//	public void updateWatchFile(Properties props) {
//		//
//	}
//
//	@Override
//	public boolean getContinueFlag() {
//		return false;
//	}

}
