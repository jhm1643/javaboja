package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TUCWebNotice {
	private static String TUCWebNotice_Header = "TUCWebNotice";
	private static String TUCWebNotice_IP = "127.0.0.1";
	private static int TUCWebNotice_PORT = 9988;

	public TUCWebNotice() {
	}

	public TUCWebNotice(String serverIp, int serverPort) {
		TUCWebNotice_IP = serverIp;
		TUCWebNotice_PORT = serverPort;
	}

	public void Send(String recvIDs, String msg, String linkURL) {
		// TODO Auto-generated method stub
		Socket s = null;
		InputStream is = null;
		BufferedWriter bw = null;
		BufferedReader br = null;
		try {
			String strMsg = msg + ((char) 15) + linkURL;
			s = new Socket(TUCWebNotice_IP, TUCWebNotice_PORT);
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			String conStr = br.readLine();
			
			bw.write(TUCWebNotice_Header + ((char) 20) + "@admin" + ((char) 20) + recvIDs + ((char) 20) + strMsg
					+ "\r\n");
			bw.flush();
			// conStr = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
				if (br != null)
					br.close();
				if (bw != null)
					bw.close();
				if (s != null)
					s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
/*	
	public static void main(String args[]) {
		
		System.out.println("# start test");
		
		new TUCWebNotice().Send("peter", "특수문자테스트 ~`!@#$%^&*()><><}{][;':\",./\\", "http://123.123.123.123/ijaowfijoaw/aefijawoefij/awejifoawoejif");
		
		for (int i = 0; i<20 ; i++) {
//			new TUCWebNotice().Send(String.format("test%03d", i), "쪽지 title#"+i, "http://123.123.123.123/ijaowfijoaw/aefijawoefij/awejifoawoejif");
		}
		System.out.println("# end test");
	}
*/
}