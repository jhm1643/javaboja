package api.utils;

import java.security.MessageDigest;

public class StringUtils {

	public static String getSHA1Hex(String s) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String ch = Integer.toHexString(0xff & messageDigest[i]);
				if (ch.length()<2) {
					hexString.append("0");
				}
				hexString.append(ch);
			}
			return hexString.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getXOR(String s1, String s2) {

//		logger.info("s1 : {}", s1);
//		logger.info("s2 : {}", s2);

		StringBuilder sb = new StringBuilder();

		if (s1.length() == s2.length()) {

			for (int i = 0; i < s1.length(); ++i) {
				
				int c1 = Integer.parseInt(String.valueOf(s1.charAt(i)), 16) & 0xf;
				int c2 = Integer.parseInt(String.valueOf(s2.charAt(i)), 16) & 0xf;

				
//				 System.out.println( Integer.toBinaryString(c1) + " XOR " + Integer.toBinaryString(c2));
//				 System.out.println(c1 + " XOR "+ c2);
//				 System.out.println("					 => "+ Integer.toHexString((c1 ^ c2)));
//				 System.out.println("					 => "+ (c1 ^ c2));

				sb.append(Integer.toHexString((c1 ^ c2)));
			}
		}
		return sb.toString();
	}

	public static String getMD5(String str) {
		String MD5 = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			MD5 = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
			MD5 = null;
		}

		return MD5;
	}
	
	/*
	public static void main(String args[]) {
		
		//calculated hash = SHA1 hexdigest ( username + “:” + nonce + “:” + timestamp + “:” password hash)
		System.out.println(getSHA1Hex("cshwang:111111:123456:abcdef1234"));
		//Auth_value : IPECS-AUTH user=ems_uce_user, nonce=4192639115308, timestamp=1433933420000, hash=d3ad582f1bf9f0e8269312ec3949e9ee7f6e2124
		System.out.println(getSHA1Hex("ems_uce_user:4192639115308:1433933420000:11216e48f88f48d76545ab2cca44be25daff13"));
	}
	 */
}
