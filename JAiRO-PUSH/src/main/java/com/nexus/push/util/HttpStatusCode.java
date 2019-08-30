package com.nexus.push.util;

public class HttpStatusCode {

	public static final String PUSH_SUCCESS = "SUCCESS";
	public static final String PUSH_FAIL = "FAIL";
	
	public static final int STATUS_200_CODE = 200;
	public static final int STATUS_400_CODE = 400;
	public static final int STATUS_500_CODE = 500;
	
	public static final String CODE_400_DATA_ERROR="Received data is null";
	public static final String CODE_400_DEVICE_ERROR="Device name is android or ios";
	public static final String CODE_400_TOKEN_ERROR="Token value is empty";
	
	public static final String CODE_500_LOCATION_ERROR="Location is not exist";
	public static final String CODE_500_LANGUAGE_ERROR="Language is not exist";
	
}
