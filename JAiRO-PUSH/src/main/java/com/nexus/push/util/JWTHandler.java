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
import com.nexus.push.domain.PushRequestVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTHandler{


	//HTTP v1 방식일 경우 해당 메소드를 통해 인증을 함
	//private int fcmTokenSet_retry = 5;
	private int fcmTokenSet_tryCount=0;
	
	public void fcmTokenSet(PushRequestVo pushDomain){
	
		try {
			logger.info("FCM TOKEN MAKE START !!!!!");
			Resource resource = new ClassPathResource(pushDomain.getKeyFile_name());
			InputStream is = resource.getInputStream();
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
				if(fcmTokenSet_tryCount == 10) e.printStackTrace();
				fcmTokenSet_tryCount++;
        		logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),fcmTokenSet_tryCount);
        		try {
        			Thread.sleep(3000);
        		}catch(InterruptedException e1) {
        			e.printStackTrace();
        		}
        		
        		fcmTokenSet(pushDomain);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String getFcmToken(String keyFile_name){
		GoogleCredential googleCredential = null;
		try {
			logger.info("FCM TOKEN MAKE START !!!!!");
			Resource resource = new ClassPathResource(keyFile_name);
			InputStream is = resource.getInputStream();
			googleCredential = GoogleCredential.fromStream(is)
				      						   .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging", 
				      								   					   "https://www.googleapis.com/auth/cloud-platform"));
			googleCredential.refreshToken();
			logger.info("FCM TOKEN : {}",googleCredential.getAccessToken());
			logger.info("FCM TOKEN MAKE END !!!!!");
		//네트워크 환경으로 인해 oauth2 토큰 인증 연결 실패시 10회 재시도
		}catch(UnknownHostException |ConnectException e) {
				if(fcmTokenSet_tryCount == 10) e.printStackTrace();
				fcmTokenSet_tryCount++;
	    		logger.info("Connection Fail Reason{}, TRY COUNT : {}",e.getMessage(),fcmTokenSet_tryCount);
	    		try {
	    			Thread.sleep(3000);
	    		}catch(InterruptedException e1) {
	    			e1.printStackTrace();
	    		}
	    		getFcmToken(keyFile_name);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return googleCredential.getAccessToken();
	}
	
	//APNS p8 토큰 인증 방식
	public void apnsTokenSet(PushRequestVo pushDomain) {
		logger.info("APNS TOKEN MAKE START !!!!!");
		long nowMillis = System.currentTimeMillis()/1000;
		String jwt_header	= String.format("{\"alg\" : \"ES256\" , \"kid\":\"%s\"}", pushDomain.getKey_id());
		String jwt_payload 	= String.format("{\"iss\" : \"%s\" , \"iat\":\"%s\"}", pushDomain.getTeam_id(), nowMillis) ;
		String base64_header = new String(Base64.encodeBase64String(jwt_header.getBytes(StandardCharsets.UTF_8)));
        String base64_payload = new String(Base64.encodeBase64String(jwt_payload.getBytes(StandardCharsets.UTF_8)));
        
        String part1 = base64_header + "." + base64_payload;
        Resource resource = new ClassPathResource(pushDomain.getKeyFile_name());
        InputStream is = null;
        BufferedReader br = null;
        String currentLine="";
        String secret="";
        String token="";
        try {
			is = resource.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
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
	
	public String getApnsToken(String key_id, String team_id, String keyFile_name) {
		logger.info("APNS TOKEN MAKE START !!!!!");
		long nowMillis = System.currentTimeMillis()/1000;
		String jwt_header	= String.format("{\"alg\" : \"ES256\" , \"kid\":\"%s\"}", key_id);
		String jwt_payload 	= String.format("{\"iss\" : \"%s\" , \"iat\":\"%s\"}", team_id, nowMillis) ;
		String base64_header = new String(Base64.encodeBase64String(jwt_header.getBytes(StandardCharsets.UTF_8)));
        String base64_payload = new String(Base64.encodeBase64String(jwt_payload.getBytes(StandardCharsets.UTF_8)));
        
        String part1 = base64_header + "." + base64_payload;
        Resource resource = new ClassPathResource(keyFile_name);
        InputStream is = null;
        BufferedReader br = null;
        String currentLine="";
        String secret="";
        String token="";
        try {
			is = resource.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
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
        logger.info("APNS TOKEN MAKE END !!!!!");
        return token;
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
