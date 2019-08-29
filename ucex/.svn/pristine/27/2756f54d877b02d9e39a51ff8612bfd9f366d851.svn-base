package api.utils;

import java.nio.charset.Charset;
import java.security.Security;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;

 

/**
 * Created by Carrey on 2018-06-20.
 */
//http://egloos.zum.com/mightyfine/v/320671

//2018.12.07 martino 
//	static NCrypto.IS_ENCRYPT, NCrypto.getEncrypted()을 사용하면 오동작이 발생하여 더이상 사용 않음

public class NCrypto {
/*
	//2018.11.01 martino 로그 암호화 여부 설정 (실제 patch에는 true로 설정해야 하며, 로그 확인 위해서는 false)
    public  static boolean IS_ENCRYPT = true; // 암호화 사용: true, 암호화 사용하지않음:false
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String KEY = "0123456789nexus0123456789nexus01";   // 256비트이므로 32바이트 문자열이 필요

    private static final String IV = "0123456789nexus0123456789nexus01";    // 256비트이므로 32바이트 문자열이 필요

    static {
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static String getEncrypted(String data) {
        try {
        	RijndaelEngine rijndael = new RijndaelEngine(256);
        	CBCBlockCipher cbc_rijndael = new CBCBlockCipher(rijndael);
        	ZeroBytePadding c = new ZeroBytePadding();
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc_rijndael, c);
            CipherParameters keyWithIV = new ParametersWithIV(new KeyParameter(KEY.getBytes()), IV.getBytes());
            cipher.init(true, keyWithIV);
            byte[] plaintext = data.getBytes(Charset.forName("UTF-8"));
            byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
            int offset = 0;
            offset += cipher.processBytes(plaintext, 0, plaintext.length, ciphertext, offset);
            cipher.doFinal(ciphertext, offset);
            String new_text = new String(new Base64().encode(ciphertext), Charset.forName("UTF-8"));
            return new_text;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public static String getDecrypted(String data) {
    	try {
            byte[] ciphertext = Base64.decode(data.trim());
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new RijndaelEngine(256)), new ZeroBytePadding());
            CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(KEY.getBytes()), IV.getBytes());
            cipher.init(false, ivAndKey);
            int minSize = cipher.getOutputSize(ciphertext.length);
            byte[] outBuf = new byte[minSize];
            int length1 = cipher.processBytes(ciphertext, 0, ciphertext.length, outBuf, 0);
            int length2 = cipher.doFinal(outBuf, length1);
            int actualLength = length1 + length2;
            byte[] cipherArray = new byte[actualLength];
            for (int x = 0; x < actualLength; x++) {
                cipherArray[x] = outBuf[x];
            }
            return new String(cipherArray);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
*/
}
