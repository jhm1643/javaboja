package api.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchResult extends AbstractProcess implements Runnable {
	private static Logger	log = LoggerFactory.getLogger(WatchResult.class);
	
	private static final long	WATCH_DURATION_MS = 500L;
	
	private String	watchPath = "";
	private String	watchFile = "";
	private String	watchFileFullPath = "";
	
	private IDataBackupTask	taskObj = null;

	//-----------------------------------------------------------------------------------------------------------------
	public WatchResult() {
		//
	}

	public WatchResult(final String watchPath, final String watchFile, final IDataBackupTask taskObj) {
		setWatchFile(watchPath, watchFile);
		this.taskObj = taskObj;
	}

	public void setWatchFile(final String watchPath, final String watchFile) {
		this.watchPath = watchPath;
		this.watchFile = watchFile;
		
		this.watchFileFullPath = watchPath + File.separator + watchFile;
	}

	//-----------------------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		initialize();
		setProcessState(EProcessState.STARTED);

		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();

			Path dir = Paths.get(watchPath);
			dir.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY );
			
			log.info("Watch Service registered for dir: " + dir.getFileName());
			
			while (true) {
				WatchKey	key = null;

				try {
					// wait for a key to be available
					key = watcher.take();
				} catch (InterruptedException ex) {
					setProcessState(EProcessState.FAILURE);
					return;
				}

				List<WatchEvent<?>> events = key.pollEvents();
				for (WatchEvent<?> event : events) {
					WatchEvent.Kind<?> kind = event.kind();
					
					// get file name
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = (Path) ev.context();
					
					//log.trace(kind.name() + " : " + fileName);
					
					if (kind == StandardWatchEventKinds.ENTRY_MODIFY && fileName.toString().equals(watchFile)) {
						log.info("File has changed!!! [" + watchFile + "]");
						read();
					}
				}
				
				try {
					Thread.sleep(WATCH_DURATION_MS);
				} catch (InterruptedException e) {}
				
				// IMPORTANT: The key must be reset after processed
				boolean valid = key.reset();
				if (!valid) {
					log.info("Key has been unregisterede");
					break;
				}
			}

			setProcessState(EProcessState.END);
		} catch (IOException e) {
			setProcessState(EProcessState.FAILURE);

			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	private void read() {
		if(taskObj == null) {
			return;
		}

		Properties props = null;
		FileInputStream fis = null;
		InputStream	bis = null;

		try{
			fis = new FileInputStream(watchFileFullPath);
			bis = new java.io.BufferedInputStream(fis);

			props = new Properties();
			props.load(bis);

			Enumeration<Object> enumeration = props.keys();
			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				log.debug("key=" + key + ", value=" + props.get(key));
			}
			
			if(!props.isEmpty()) {
				log.debug("props " + props);
				taskObj.updateWatchFile(props);
			}
		} catch(Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		} finally {
			if(bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				bis = null;
			}

			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fis = null;
			}
		}
	}

}
