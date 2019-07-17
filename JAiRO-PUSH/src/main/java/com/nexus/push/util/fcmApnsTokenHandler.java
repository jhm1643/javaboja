package com.nexus.push.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Joiner;
import com.nexus.push.domain.PushDomain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class fcmApnsTokenHandler{


	//HTTP v1 방식일 경우 해당 메소드를 통해 인증을 함
	//private int fcmTokenSet_retry = 5;
	private int fcmTokenSet_tryCount=0;
	
	public void fcmTokenSet(PushDomain pushDomain, ServletContext servletContext) throws IOException, InterruptedException{
	
		try {
			logger.info("FCM TOKEN MAKE START !!!!!");
			Resource resource = new ClassPathResource(pushDomain.getKeyFile_name());
			InputStream is = resource.getInputStream();
			String keyRealPath = "resources/"+pushDomain.getKeyFile_name();
			GoogleCredential googleCredential = GoogleCredential
				      .fromStream(is)
				      .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging", 
				    		  "https://www.googleapis.com/auth/cloud-platform"));
			googleCredential.refreshToken();
			logger.info("FCM TOKEN : {}",googleCredential.getAccessToken());
			pushDomain.setServer_token(googleCredential.getAccessToken());
			logger.info("FCM TOKEN MAKE END !!!!!");
			
		//네트워크 환경으로 인해 oauth2 토큰 인증 연결 실패시 10회 재시도
		}catch(UnknownHostException |ConnectException e) {
				if(fcmTokenSet_tryCount == 10) throw e;
				fcmTokenSet_tryCount++;
        		logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),fcmTokenSet_tryCount);
        		Thread.sleep(3000);
        		fcmTokenSet(pushDomain, servletContext);
		}
		
	}
	
	//APNS p8 토큰 인증 방식
	public void apnsTokenSet(PushDomain pushDomain, ServletContext servletContext) {
		logger.info("APNS TOKEN MAKE START !!!!!");
		long nowMillis = System.currentTimeMillis()/1000;
		String jwt_header	= String.format("{\"alg\" : \"ES256\" , \"kid\":\"%s\"}", pushDomain.getKey_id());
		String jwt_payload 	= String.format("{\"iss\" : \"%s\" , \"iat\":\"%s\"}", pushDomain.getTeam_id(), nowMillis) ;
		String keyRealPath = servletContext.getRealPath(pushDomain.getKey_path())+"/"+pushDomain.getKeyFile_name();
		String base64_header = new String(Base64.encodeBase64String(jwt_header.getBytes(StandardCharsets.UTF_8)));
        String base64_payload = new String(Base64.encodeBase64String(jwt_payload.getBytes(StandardCharsets.UTF_8)));
        
        String part1 = base64_header + "." + base64_payload;
        String token="";
        BufferedReader br = null;
        InputStreamReader isr = null;
        String secret="";
        try {
            String currentLine;
            Resource resource = new ClassPathResource(pushDomain.getKeyFile_name());
			InputStream is = resource.getInputStream();
            isr = new InputStreamReader(is);
			File file=new File(keyRealPath);
            br = new BufferedReader(isr);
            while ((currentLine = br.readLine()) != null) {
            	if(currentLine.contains("BEGIN PRIVATE KEY") || currentLine.contains("END PRIVATE KEY")) {
            		continue;
            	}
                secret+=currentLine;
            }
           br.close();
           token = base64_header + "." + base64_payload + "." + ES256(secret, part1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("APNS TOKEN : {}",token);
        pushDomain.setServer_token(token);
        logger.info("APNS TOKEN MAKE END !!!!!");
	}
	
	public String ES256(final String secret, final String data) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		 
        KeyFactory kf;

        kf = KeyFactory.getInstance("EC");
        KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64((secret.getBytes(StandardCharsets.UTF_8))));
        PrivateKey key;
        key = kf.generatePrivate(keySpec);

        final Signature sha256withECDSA = Signature.getInstance("SHA256withECDSA");
        sha256withECDSA.initSign(key);

        sha256withECDSA.update(data.getBytes(StandardCharsets.UTF_8));
        final byte[] signed = sha256withECDSA.sign();
        
        return Base64.encodeBase64String(signed);
           
	}
}
