package api.process;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static Logger	log = LoggerFactory.getLogger(Utils.class);

	public static List<String> getLocalIpAddress() {
		List<String>	ipAddrs = new ArrayList<String>();

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						//return inetAddress.getHostAddress().toString();
						String	ipAddr = inetAddress.getHostAddress().toString();
						log.debug(ipAddr);
						ipAddrs.add(ipAddr);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return ipAddrs;
	}

	public static boolean createDir(final String directoryName) {
		/*
		File theDir = new File("new folder");

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			log.info("creating directory: " + directoryName);
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		    	se.printStackTrace();
		    	log.error(se.getMessage());
		    }        
		    if(result) {    
		    	log.info("DIR created");  
		    }
		    return result;
		}
		return true;
		*/
		
		log.debug("creating directory: " + directoryName);

		File theDir = new File(directoryName);

		try {
			// if the directory does not exist, create it
			if (! theDir.exists() ) {
				//Files.createDirectories(Paths.get(directoryName));
				theDir.mkdirs();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return false;
	}


	//-----------------------------------------------------------------------------------------------------------------
	/**
	 * Process의 PID를 읽는다.
	 * @param prc
	 * @return
	 */
	public static long getPidOfProcess(Process prc) {
		long	pid = -1;

		if(prc == null) {
			return pid;
		}

		try {
			// UNIX process인 것만
			if(prc.getClass().getName().equals("java.lang.UNIXProcess")) {
				Field	fld = prc.getClass().getDeclaredField("pid");
				fld.setAccessible(true);
				pid = fld.getLong(prc);
				fld.setAccessible(false);
			}
		} catch (Exception e) {
			pid = -1;
			e.printStackTrace();
			log.error(e.toString());
		}
		
		return pid;
	}

}
