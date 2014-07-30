package com.example.energylens;

import android.content.SharedPreferences;

public class Common{
	public static long INTERVAL = 30; //seconds between each scheduling of service
	public static int SAMPLE_TIME=10; //seconds for which sensors will take data
	public static String LABEL="none";
	public static String LOCATION="none";
	public static long UPLOAD_INTERVAL = 2; //minutes between each upload
	public static String FILE_PREFIX="";
	
	public static void changeLabel(String newLabel){
		LABEL=newLabel; 
		// Restore preferences
	}
	
	public static void changeLocation(String newLocation){
		LOCATION=newLocation; 
		// Restore preferences
	}
	
	public static void changePrefix(String prefix){
		if(prefix!=""){
			FILE_PREFIX=prefix+"_";
		}
		else{
			FILE_PREFIX=prefix;
		}
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
