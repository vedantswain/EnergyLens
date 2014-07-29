package com.example.energylens;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class LogWriter {
	public static File axlLog, errorLog, wifiLog,audioLog,rawaudioLog,lightLog,magLog; 
	
	public static File EnergyLensDir=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+");
	
	
	public static void PathCheck(File logFile){
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
			try {
				Log.v("LOG WRITER","file created at: "+logFile.getPath());
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.print(e.toString());
				if(logFile!=errorLog)
					Log.v("LOG WRITER",e.toString());
					errorLogWrite(e.toString());
			}
		}
	}

	public static void LogWrite(File logFile,String logstring){
		synchronized(logFile){
			
					PathCheck(logFile);

					BufferedWriter buf;
					try {
						buf = new BufferedWriter(new FileWriter(logFile, true));
						buf.append(logstring);
						buf.newLine();
						buf.close();
						//Log.v("ELSERVICES", "written " + logstring +" into"+logFile.toString() );
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
		axlLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"accelerometer_log"+".csv");
		LogWrite(axlLog,logstring);
	}
	
	public static void audioLogWrite(String logstring){
		audioLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"audio_log"+".csv");
		LogWrite(audioLog,logstring);
	}
	
	public static void rawaudioLogWrite(String logstring){
		rawaudioLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"rawaudio_log"+".csv");
		LogWrite(rawaudioLog,logstring);
	}
	
	public static void lightLogWrite(String logstring){
		lightLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"light_log"+".csv");
		LogWrite(lightLog,logstring);
	}
	
	public static void magLogWrite(String logstring){
		magLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"mag_log"+".csv");
		LogWrite(magLog,logstring);
	}
	
	public static void wifiLogWrite(String logstring){
		wifiLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"wifi_log"+".csv");
		LogWrite(wifiLog,logstring);
	}
	
	public static void errorLogWrite(String logstring){
		errorLog=new File(Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator+"error_log"+".csv");
		LogWrite(errorLog,logstring);
	}
	
}
