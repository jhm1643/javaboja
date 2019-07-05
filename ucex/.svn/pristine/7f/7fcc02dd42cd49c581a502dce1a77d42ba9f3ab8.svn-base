/**
 * 
 */
package api.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author arthur
 *
 */
public class SysCall {
	private static final Logger	logger = LoggerFactory.getLogger(SysCall.class);
	
	public static final String	EXTERNAL_CMD_SU = "/bin/su";
	
	private	Thread			mThrJobWorker = null;
	private	String			mJobThreadName = "";
	// Exit waiting 
	private	CountDownLatch	mCountDown = null;
	// StdOut, StdError output buffer
	private StringBuffer	mStdOut = new StringBuffer();
	private StringBuffer	mStdErr = new StringBuffer();
	private	Thread			mThrOutReader = null;
	private	Thread			mThrErrReader = null;
	
	private int				mRetWaitForValue = -1;
	private int				mExitValue = -1;
	private long			mRunProcessPid = -1L;

	//---------------------------------------------------------------------------------
	public SysCall() {
		mJobThreadName = "SysCall" + UUID.randomUUID().toString();
	}

	@Override
	protected void finalize() throws Throwable {
		countDown();

		workerInterrupt(mThrOutReader);
		workerInterrupt(mThrErrReader);
		workerInterrupt(mThrJobWorker);

		workerDestroy(mThrOutReader);
		mThrOutReader = null;
	
		workerDestroy(mThrErrReader);
		mThrErrReader = null;

		workerDestroy(mThrJobWorker);
		mThrJobWorker = null;

		super.finalize();
	}

	//---------------------------------------------------------------------------------
	public String getStdOutBuf() {
		return mStdOut.toString();
	}
	
	public String getStdErrBuf() {
		return mStdErr.toString();
	}

	public int getExitValue() {
		return mExitValue;
	}

	public int getWaitForValue() {
		return mRetWaitForValue;
	}

	public long getRunProcessPid() {
		return mRunProcessPid;
	}

	//---------------------------------------------------------------------------------
	private void newCountDown() {
		if( mCountDown != null ) {
			mCountDown.countDown();
			mCountDown = null;
		}
		mCountDown = new CountDownLatch(1);
	}

	private void countDown() {
		if( mCountDown != null ) {
			mCountDown.countDown();
			mCountDown = null;
		}
	}
	
	public int await(boolean bForceFlag, final long waitMilliseconds) {
		if(bForceFlag) {
			countDown();
			return 0;
		} else {
			try {
				if(waitMilliseconds > 0L) {
					mCountDown.await(waitMilliseconds, TimeUnit.MILLISECONDS);
				} else {
					mCountDown.await();					
				}
			} catch (InterruptedException e) {
				//if( mStdErr.length() > 0 ) { mStdErr.append("\n"); }
				//mStdErr.append(e.toString());
				logger.error(mJobThreadName + " : " + e);
			} finally {
				countDown();
			}
			
			logger.info(mJobThreadName + " : " + mExitValue);
		}
		
		return mExitValue;
	}

	//---------------------------------------------------------------------------------
	public void start(final String userId, final String command) {
		final List<String>	cmds = new ArrayList<String>();
		cmds.add(command);

		start(userId, cmds);
	}

	public void start(final String userId, final List<String> commands) {
		newCountDown();

		logger.info(mJobThreadName + " user='" + userId + "' cmd=[" + commands.toString() + "]");
		
		mThrJobWorker = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mExitValue = startRunProcess(userId, commands);
				} catch (Exception e) {
					mExitValue = -1;
					logger.error(mJobThreadName + " user='" + userId + "' cmd=[" + commands.toString() + "] " + e);

					if( mStdErr.length() < 1 ) {
						mStdErr.append(mJobThreadName + " user='" + userId + "' cmd=[" + commands.toString() + "] " + e.toString());
					}
				} finally {
					countDown();
				}

				logger.debug(String.format("%s RESULT=%d\nSTDOUT=[%s]\nSTDERR=[%s]\n",
						mJobThreadName, mExitValue, getStdOutBuf(), getStdErrBuf())
						);
			}
		});
		
		mThrJobWorker.setName(mJobThreadName);
		mThrJobWorker.start();
	}
	
	private void workerInterrupt(final Thread thread) {
		if( thread != null ) {
			thread.interrupt();
		}
	}
	
	private void workerDestroy(Thread thread) {
		if( thread != null ) {
			while( ! thread.isInterrupted() ) {
				try {
					Thread.sleep(50);
				} catch(Exception e) {}
			}
			thread = null;
		}
	}

	//---------------------------------------------------------------------------------
	private int startRunProcess(final String userId, final List<String> commands) throws Exception {
		List<String> cmd = new ArrayList<String>();
		
		if( userId == null || userId.isEmpty() ) {
			cmd.add(EXTERNAL_CMD_SU);
			cmd.add("-c");
			cmd.addAll(commands);

			logger.info(mJobThreadName + " cmd=[" + commands + "]");
		} else {
			cmd.add(EXTERNAL_CMD_SU);
			cmd.add("-");
			cmd.add(userId);
			//cmd.add("--login");
			cmd.add("-c");
			cmd.addAll(commands);

			logger.info(mJobThreadName + " user='" + userId + "' cmd=[" + commands + "]");
		}

		return startRunProcess(cmd);
	}

	private int startRunProcess(final List<String> commands) throws Exception {
		Exception	errRuntime = null;
		
		ProcessBuilder pb = null;
		Process runRrocess = null;
		
		InputStream	outStream = null;
		InputStream	errStream = null;
		
		mExitValue = -1;

		try {
			pb = new ProcessBuilder(commands);
			runRrocess = pb.start();

			mRunProcessPid = Utils.getPidOfProcess(runRrocess);
			
			outStream = runRrocess.getInputStream();
			errStream = runRrocess.getErrorStream();
			
			startOutReader(outStream);
			startErrReader(errStream);
			
			mRetWaitForValue = runRrocess.waitFor();
		} catch(Exception e) {
			errRuntime = e;
			logger.error(mJobThreadName + " : " + e);
			
			String[]	errMsg = e.toString().split(":");
			if( errMsg != null && errMsg.length > 0 ) {
				for(int i = 1; i < errMsg.length; i++) {
					if(mStdErr.length() < 1 ) {
						mStdErr.append( errMsg[i].trim() );
					} else {
						mStdErr.append( "\n" );
						mStdErr.append( errMsg[i].trim() );
					}
				}
			}
		} finally {
			stopOutReader();
			stopErrReader();

			closeInputStream(outStream);
			closeInputStream(errStream);

			if( runRrocess != null ) {
				try {
					mExitValue = runRrocess.exitValue();
				} catch(Exception e) {
					logger.error(mJobThreadName + " : " + e);
				}

				runRrocess.destroy();
			}
			runRrocess = null;
			
			if( pb != null ) {
				pb = null;
			}
		}
		
		if( errRuntime != null ) {
			throw new RuntimeException(errRuntime);
		}
		
		return mExitValue;
	}

	private void closeInputStream(InputStream is) {
		try {
			if( is != null ) {
				is.close();
			}
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		} finally {
			is = null;	
		}
	}

	private void closeBufferedReader(BufferedReader br) {
		try {
			if( br != null ) {
				br.close();
			}
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		} finally {
			br = null;	
		}
	}

	private void closeInputStreamReader(InputStreamReader isr) {
		try {
			if( isr != null ) {
				isr.close();
			}
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		} finally {
			isr = null;
		}
	}

	//---------------------------------------------------------------------------------
	private void startOutReader(final InputStream stream) {
		mStdOut.delete(0, mStdOut.length());
		
		try {
			mThrOutReader = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						readBuffer(stream, mStdOut);
					} catch (Exception e) {
						logger.error(mJobThreadName + " : " + e);
					}
				}
				
			});
			
			mThrOutReader.setName(mJobThreadName + "-READER-STDOUT");
			mThrOutReader.start();
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		}
	}
	
	private void startErrReader(final InputStream stream) {
		mStdErr.delete(0, mStdErr.length());
		
		try {
			mThrErrReader = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						readBuffer(stream, mStdErr);
					} catch (Exception e) {
						logger.error(mJobThreadName + " : " + e);
					}
				}
				
			});
			
			mThrErrReader.setName(mJobThreadName + "-READER-STDERR");
			mThrErrReader.start();
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		}
	}

	private void stopOutReader() {
		try {
			if( mThrOutReader != null ) {
				mThrOutReader.interrupt();
			}
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		} finally {
			mThrOutReader = null;
		}
	}

	private void stopErrReader() {
		try {
			if( mThrErrReader != null ) {
				mThrErrReader.interrupt();
			}
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		} finally {
			mThrErrReader = null;
		}
	}

	//---------------------------------------------------------------------------------
	private void readBuffer(final InputStream stream, final StringBuffer sbBuf) {
		InputStreamReader	streamReader = null;
		BufferedReader		bufReader = null;

		try {
			streamReader = new InputStreamReader(stream);
			bufReader = new BufferedReader(streamReader);
			
			String	readLine = null;
			boolean	readed = false;
			
			// read the output from the command
			while ( Thread.currentThread().isAlive() &&
					!Thread.currentThread().isInterrupted() &&
					(readLine = bufReader.readLine()) != null )
			{
				//logger.debug(readLine);
				if( readed ) {
					sbBuf.append("\n");
				} else {
					readed = true;
				}
				sbBuf.append(readLine);
			}
	
			//if( sbBuf.length() > 0 ) {
			//	//logger.debug(mJobThreadName + " : " + "[" + Thread.currentThread().getName() +"] " + sbBuf.toString());
			//}
		} catch(Exception e) {
			logger.error(mJobThreadName + " : " + e);
		} finally {
			closeBufferedReader(bufReader);
			closeInputStreamReader(streamReader);
		}
	}

	//---------------------------------------------------------------------------------
	// cmd example : "echo ~" + userName
	public String testMain(final String cmd) throws IOException, InterruptedException{
		String[]	cmds = new String[]{"sh", "-c", cmd};
		
		Process	outsideProcess = Runtime.getRuntime().exec(cmds);
		
		InputStream	in = outsideProcess.getInputStream();
		InputStreamReader	inReader = new InputStreamReader(in);
		BufferedReader	bufReader = new BufferedReader(inReader);
		
		StringBuilder sb = new StringBuilder();
		String readLine;
		while((readLine = bufReader.readLine()) != null) {
			sb.append(readLine);
		}

		outsideProcess.waitFor();

		bufReader.close();
		inReader.close();
		in.close();
		
		outsideProcess.destroy();

	    return sb.toString().trim();
	}

	//---------------------------------------------------------------------------------
	/* TEST
	public static void main(final String args[]) {
		SysCall	sys = new SysCall();
		
		try {
			System.out.println("result=" + sys.testMain("echo ~cmuc"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("result=" + sys.testMain("ping -c 5 www.google.co.kr"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		List<String> commands = new ArrayList<String>();
		commands.add("ping");
		commands.add((isWindows? "-n" : "-c"));
		commands.add("5");
		commands.add("www.google.co.kr");
		
		try {
			sys.start("", commands);
			int	exitValue = sys.await(false, 0L);
			System.out.println("exitValue=" + exitValue);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.toString());
		}
	}
	*/

}
