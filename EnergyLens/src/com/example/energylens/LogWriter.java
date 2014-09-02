package com.example.energylens;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class LogWriter {
	public static File axlLog, errorLog, wifiLog,audioLog,rawaudioLog,lightLog,magLog; 
	public static String WIFIHEADER="time"+","+"mac"+","+"ssid"+","+"rssi"+","+"label";
	public static String ACCLHEADER="time"+","+"x"+","+"y"+","+"z"+","+"label"+","+"location";
	public static String RAWSOUNDHEADER="time"+","+"values"+","+"label"+","+"location";
	public static String SOUNDHEADER="time"+","+"mfcc1"+","+"mfcc2"+","+"mfcc3"+","+"mfcc4"+","+"mfcc5"+","+"mfcc6"+","+"mfcc7"+","+"mfcc8"+","+"mfcc9"+","+"mfcc10"+","+"mfcc11"+","+"mfcc12"+","+"mfcc13"+","+"label"+","+"location";
	public static String LIGHTHEADER = "time" + "," + "value" + "," + "label" +","+"location";
	public static String MAGHEADER = "time" + "," + "x" + "," + "y" + "," + "z" + ","+"label"+","+"location"; 
	public static String ERRHEADER = "error log";
	
	public static File EnergyLensDir=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+");
	
		
	public static void PathCheck(File logFile,String header){
//		Log.v("ELSERVICES", "checking path");
		if(!EnergyLensDir.exists()){
			try{
			EnergyLensDir.mkdir();
			Log.v("LOG WRITER","directory made at: "+EnergyLensDir.getPath());
			}
			catch(Exception e){
				Log.v("LOG WRITER",e.toString());
			}
		}
		
		if(!logFile.exists()){
			BufferedWriter buf;
			try {
				Log.v("LOG WRITER","file created at: "+logFile.getPath());
				logFile.createNewFile();
				buf = new BufferedWriter(new FileWriter(logFile, true));
				buf.append(header);
				buf.newLine();
				buf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.print(e.toString());
				if(logFile!=errorLog)
					Log.v("LOG WRITER",e.toString());
					errorLogWrite(e.toString());
			}
		}
	}

	public static void LogWrite(File logFile,String logstring,String header,boolean isWifi,boolean isAudio){
		synchronized(logFile){
					
//					Log.v("ELSERVICES", "Writing Log to: "+logFile.toString()+" "+Common.LABEL);
					PathCheck(logFile,header);
					
					BufferedWriter buf;
					try {
						buf = new BufferedWriter(new FileWriter(logFile, true));
//						Log.v("ELSERVICES", "Before label");
						if(isWifi==true)
							buf.append(logstring+","+Common.LOCATION);
						else if(isAudio==true){
							StringBuilder logstr=new StringBuilder(logstring);
							logstr.replace(logstr.length()-1, logstr.length(), "");
							logstring=logstr.toString();
							buf.append(logstring);
						}
						else
							buf.append(logstring+","+Common.LABEL+","+Common.LOCATION);
						buf.newLine();
						buf.close();
//						Log.v("ELSERVICES", logstring+" written  into"+logFile.toString() );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.v("LOG WRITER",e.toString());
						if(logFile!=errorLog)
							errorLogWrite(e.toString());
					} 
				
			}
	}		

	
	public static void axlLogWrite(String logstring){
		axlLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+Common.FILE_PREFIX+"accelerometer_log"+".csv");
		LogWrite(axlLog,logstring,ACCLHEADER,false,false);
	}
	
	public static void audioLogWrite(String logstring){
		audioLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+Common.FILE_PREFIX+"audio_log"+".csv");
		LogWrite(audioLog,logstring,SOUNDHEADER,false,true);
	}
	
	public static void rawaudioLogWrite(String logstring){
		rawaudioLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+Common.FILE_PREFIX+"rawaudio_log"+".csv");
		LogWrite(rawaudioLog,logstring,RAWSOUNDHEADER,false,true);
	}
	
	public static void lightLogWrite(String logstring){
		lightLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+Common.FILE_PREFIX+"light_log"+".csv");
		LogWrite(lightLog,logstring,LIGHTHEADER,false,false);
	}
	
	public static void magLogWrite(String logstring){
		magLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+Common.FILE_PREFIX+"mag_log"+".csv");
		LogWrite(magLog,logstring,MAGHEADER,false,false);
	}
	
	public static void wifiLogWrite(String logstring){
		wifiLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+Common.FILE_PREFIX+"wifi_log"+".csv");
		LogWrite(wifiLog,logstring,WIFIHEADER,true,false);
	}
	
	public static void errorLogWrite(String logstring){
		errorLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"error_log"+".csv");
		LogWrite(errorLog,logstring,ERRHEADER,false,false);
	}
	
}
