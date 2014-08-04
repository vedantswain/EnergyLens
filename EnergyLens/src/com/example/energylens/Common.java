package com.example.energylens;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

public class Common{
	public static String SENDER_ID = "166229175411";
	public static long INTERVAL = 30; //seconds between each scheduling of service
	public static int SAMPLE_TIME=10; //seconds for which sensors will take data
	public static String LABEL="none";
	public static String LOCATION="none";
	public static long UPLOAD_INTERVAL = 2; //minutes between each upload
	public static String FILE_PREFIX="";
	public static String SERVER_URL="http://192.168.1.2:8080/";
	public static String API="data/upload/";
	public static String REG_API="device/register/";
	public static int TRAINING_STATUS=0;
	public static int TRAINING_COUNT=0;
	public static String EL_PREFS="EnerglyLens_Prefs";
	public static JSONObject UPLOAD_SUCCESS =  new JSONObject();
	static {try {
		UPLOAD_SUCCESS.put("type", "SUCCESS");
		UPLOAD_SUCCESS.put("code", 1);
		UPLOAD_SUCCESS.put("message", "Data was successfully uploaded");
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}};
	public static JSONObject UPLOAD_UNSUCCESSFULL =  new JSONObject();
	static {try {
		UPLOAD_UNSUCCESSFULL.put("type", "ERROR");
		UPLOAD_UNSUCCESSFULL.put("code", 2);
		UPLOAD_UNSUCCESSFULL.put("message", "Data was not uploaded");
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}};
	public static JSONObject ERROR_INVALID_REQUEST =  new JSONObject();
	static {try {
		ERROR_INVALID_REQUEST.put("type", "ERROR");
		ERROR_INVALID_REQUEST.put("code", 0);
		ERROR_INVALID_REQUEST.put("message", "Invalid request made");
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}};
	public static void changeTrainingStatus(int status){
		TRAINING_STATUS=status;
	}
	
	public static void changeTrainingCount(int newcount){
		TRAINING_COUNT=newcount;
	}
			
	public static void changeLabel(String newLabel){
		LABEL=newLabel; 
		// Restore preferences
	}
	
	public static void changeServerUrl(String newURL){
		SERVER_URL=newURL;
	}
	
	public static void changeLocation(String newLocation){
		LOCATION=newLocation; 
		// Restore preferences
	}
	
	public static void changePrefix(String prefix){
		FILE_PREFIX=prefix;
	}
	
	public static void changeInterval(long interval){
		INTERVAL=interval; 
		// Restore preferences
	}
	
	public static void changeSampleTime(int sampleTime){
		SAMPLE_TIME=sampleTime; 
		// Restore preferences
	}
	
	public static void changeUploadInterval(long interval){
		UPLOAD_INTERVAL=interval; 
		// Restore preferences
	}
	
	
}
