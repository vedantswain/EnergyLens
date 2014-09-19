package com.example.energylens;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class UploaderService extends Service{

	HttpURLConnection connection = null;
	DataOutputStream outputStream = null;
	DataInputStream inputStream = null;
	String path = Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator;
	String dataPath=path+"SensorData"+File.separator;
	String researchPath= path+"UsageStats"+File.separator;
	String [] file={"accelerometer_log.csv","audio_log.csv","light_log.csv","mag_log.csv","rawaudio_log.csv","wifi_log.csv",
			"Training_accelerometer_log.csv","Training_audio_log.csv","Training_light_log.csv","Training_mag_log.csv","Training_rawaudio_log.csv","Training_wifi_log.csv"};
	String [] researchFile={"screen_log.csv"};
	String urlServer = Common.SERVER_URL+Common.API;
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary =  "*****";

	public static String WIFIHEADER="time"+","+"mac"+","+"ssid"+","+"rssi"+","+"label";
	public static String ACCLHEADER="time"+","+"x"+","+"y"+","+"z"+","+"label"+","+"location";
	public static String RAWSOUNDHEADER="time"+","+"values"+","+"label"+","+"location";
	public static String SOUNDHEADER="time"+","+"mfcc1"+","+"mfcc2"+","+"mfcc3"+","+"mfcc4"+","+"mfcc5"+","+"mfcc6"+","+"mfcc7"+","+"mfcc8"+","+"mfcc9"+","+"mfcc10"+","+"mfcc11"+","+"mfcc12"+","+"mfcc13"+","+"label"+","+"location";
	public static String LIGHTHEADER = "time" + "," + "value" + "," + "label" +","+"location";
	public static String MAGHEADER = "time" + "," + "x" + "," + "y" + "," + "z" + ","+"label"+","+"location"; 
	public static String ERRHEADER = "error log";
	public static String SCREENHEADER="time_of_day"+","+"screen_name"+","+"time_of_stay";

	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 2*1024*1024;

	@SuppressWarnings("unused")
	private int serverResponseCode;
	@SuppressWarnings("unused")
	private String serverResponseMessage;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

	@Override
	public void onCreate() {        
		super.onCreate();

	}

	@Override
	public void onDestroy() {        
		//Log.v("ELSERVICES","axlService stopped "+System.currentTimeMillis());	
		stopSelf();
	}

	public void getUpdatedPreferences(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Common.changeServerUrl(sharedPref.getString("SERVER_URL",Common.SERVER_URL));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {  
		Log.v("ELSERVICES","Uploader started "+System.currentTimeMillis());
		getUpdatedPreferences();
		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		thr.start();

		return START_NOT_STICKY; //makes sure services don't restart on stopping

	}

	private void upload_research(){
		for(String filename:researchFile){
			research_upload_setup(filename);
		}
		Log.i("ELSERVICES", "all research files visited "+System.currentTimeMillis());
		research_upload_pending();
	}

	public void timeCheck(){
		SharedPreferences sp=getSharedPreferences(Common.EL_PREFS,0);
		long timePassed=System.currentTimeMillis()-sp.getLong("LAST_RS_UP", System.currentTimeMillis());
		Editor editor=sp.edit();
		if(timePassed==0)
			editor.putLong("LAST_RS_UP", System.currentTimeMillis());
		Log.v("ELSERVICES", "TIME CHECK: "+timePassed/(60*1000));
		if(timePassed>1*10*60*1000){
			upload_research();
			editor.putLong("LAST_RS_UP", System.currentTimeMillis());
		}
		editor.commit();
	}

	Runnable mTask = new Runnable() {
		public void run() {

			synchronized (mBinder) {
				try {

					for(String filename:file){
						upload_setup(filename);
					}
					Log.i("ELSERVICES", "all files visited "+System.currentTimeMillis());
					upload_pending();
					timeCheck();
					stopSelf();					
				}
				catch (Exception e) {
				}
			}
		}
	};

	public void fileCopy(File src,File dst){
		try {
			dst.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(src);
			output = new FileOutputStream(dst);
			byte[] buf = new byte[1024];
			int bytesRead;

			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			input.close();
			output.close();
			Log.i("ELSERVICES", "Upload File created");
			src.delete();
		}
		catch(Exception e){
			e.printStackTrace();				
		}
	}

	public String getDate() {
		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy_HH-mm-ss");
		//		   	 String unique= date.toString().replace(" ", "");
		//		   	 Log.i("ELSERVICES","path new: "+unique.replace(":",""));
		return ft.format(date);
	}

	public void research_upload_pending(){

		File list[] = (new File(researchPath)).listFiles();

		Arrays.sort(list, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			} });

		//
		for(int i=list.length-1;i>=0;i--){
			File file=list[i];
			String filename=file.getAbsolutePath().replace(researchPath, "");
			Log.i("ELSERVICES", "pending "+filename);
			Log.i("ELSERVICES", "uploading pending research files");
			if(file.length()==0)
				file.delete();
			else{
				headerCheck(file);
				upload(file,Common.SERVER_URL+Common.STATS_API);
			}
		}
	}

	public void upload_pending(){

		File list[] = (new File(dataPath)).listFiles();

		Arrays.sort(list, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			} });

		//
		for(int i=list.length-1;i>=0;i--){
			File file=list[i];
			String filename=file.getAbsolutePath().replace(dataPath, "");
			Log.i("ELSERVICES", "pending "+filename);
			if(filename.contains("upload_")){
				Log.i("ELSERVICES", "uploading pending files");
				if(file.length()==0)
					file.delete();
				else{
					headerCheck(file);
					upload(file,Common.SERVER_URL+Common.API);
				}

			}
		}
	}


	private void headerFix(File file, String header){
		String tempFilePath=dataPath+"temp_log.csv";
		Log.v("ELSERVICES",header+": Header missing");

		//fixing header

		try {
			BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath));

			bw.write(header);
			bw.write(System.getProperty("line.separator"));

			String newLine=br.readLine();
			while(newLine!=null) {
				bw.write(newLine);
				bw.write(System.getProperty("line.separator"));
				newLine=br.readLine();
			}

			br.close();
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//copying temp contents back to main file
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			writer.print("");
			writer.close();

			BufferedReader br = new BufferedReader(new FileReader(tempFilePath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));

			String newLine=br.readLine();
			while(newLine!=null) {
				bw.write(newLine);
				bw.write(System.getProperty("line.separator"));
				newLine=br.readLine();
			}

			br.close();
			bw.close();
			
			File fileTemp=new File(tempFilePath);
			fileTemp.delete();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void headerCheck(File file){
		String header="";

		if(file.getAbsolutePath().contains("wifi_log")){
			header=WIFIHEADER;
		}
		else if(file.getAbsolutePath().contains("accelerometer_log")){
			header=ACCLHEADER;
		}
		else if(file.getAbsolutePath().contains("_audio_log")){
			header=SOUNDHEADER;
		}
		else if(file.getAbsolutePath().contains("rawaudio_log")){
			header=RAWSOUNDHEADER;
		}
		else if(file.getAbsolutePath().contains("light_log")){
			header=LIGHTHEADER;
		}
		else if(file.getAbsolutePath().contains("mag_log")){
			header=MAGHEADER;
		}
		else if(file.getAbsolutePath().contains("screen_log")){
			header=SCREENHEADER;
		}

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String firstline=br.readLine();
			Log.v("ELSERVICES", "Header: "+firstline+" Second Line: "+br.readLine());

			if (!firstline.equals(header)) {
				headerFix(file,header);
				br.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     

	}

	public void research_upload_setup(String filename){
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String devId=telephonyManager.getDeviceId();
		String pathToFile=researchPath+filename;
		String[] split=filename.split("\\.");
		String uniqueID=getDate();
		String upPathToFile=researchPath+devId+'_'+split[0]+'_'+uniqueID+".csv";

		File oldFile=new File(pathToFile);
		File upFile=new File(upPathToFile);

		if(oldFile.exists()){
			fileCopy(oldFile,upFile);
			headerCheck(upFile);
			if(upFile.length()==0)
				upFile.delete();
			else{
				Log.v("ELSERVICES", "Research upload: "+oldFile.toString()+"->"+upFile.toString());
				upload(upFile,Common.SERVER_URL+Common.STATS_API);

			}
		}
	}

	public void upload_setup(String filename){
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String devId=telephonyManager.getDeviceId();
		String pathToFile=dataPath+filename;
		String[] split=filename.split("\\.");
		String uniqueID=getDate();
		String upPathToFile=dataPath+devId+'_'+"upload_"+split[0]+"_"+uniqueID+".csv"; 

		File oldFile=new File(pathToFile);
		File upFile=new File(upPathToFile);

		if(oldFile.exists()){
			fileCopy(oldFile,upFile);
			if(upFile.length()==0)
				upFile.delete();
			else
				upload(upFile,Common.SERVER_URL+Common.API);
		}
	}


	public void upload(File upFile,String URL){
		try
		{
			Log.i("ELSERVICES", URL);
			URL url = new URL(URL);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs &amp; Outputs.
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Set HTTP method to POST.
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			//	    	    Log.v("ELSERVICES",upFile.getAbsolutePath());


			FileInputStream fileInputStream = new FileInputStream(upFile);
			//		    	Log.i("ELSERVICES", upFile.getAbsolutePath());

			if(connection!=null){

				outputStream = new DataOutputStream( connection.getOutputStream() );
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" +upFile.getAbsolutePath() +"\"" + lineEnd);
				outputStream.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				Log.i("ELSERVICES", Integer.toString(bufferSize));

				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0)
				{
					outputStream.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = connection.getResponseCode();
				serverResponseMessage = connection.getResponseMessage();

				Log.v("ELSERVICES",Integer.toString(serverResponseCode)+" "+serverResponseMessage);

				fileInputStream.close();
				outputStream.flush();
				outputStream.close();

				InputStream in=null;
				StringBuffer sb=new StringBuffer();

				try {
					in=connection.getInputStream();
					int ch;
					while ((ch = in.read()) != -1) {
						sb.append((char) ch);
					}
					Log.v("ELSERVICES", "input stream: "+sb.toString());
				} catch (IOException e) {
					throw e;
				} finally {
					if (in != null) {
						in.close();
					}
				}

				JSONObject response=new JSONObject(sb.toString());
				String serverResponseType=response.getString("type");
				int serverResponseCode=response.getInt("code");
				String serverResponseMessage=response.getString("message");
				Log.v("ELSERVICES","URL: "+URL+ 
						" type: "+serverResponseType+'\n'+"code: "+serverResponseCode+'\n'+"message+: "+serverResponseMessage);

				if(connection.getResponseCode()>=200 && connection.getResponseCode()<300 && serverResponseCode==1){
					upFile.delete();	    	    
					Log.v("ELSERVICES", "Upload complete "+System.currentTimeMillis());

				}
				else{
					Log.i("ELSERVICES", "can't upload due to Code: "+Integer.toString(serverResponseCode)+serverResponseMessage);
				}
			}
			else{
				Log.v("ELSERVICES", "Upload failed "+System.currentTimeMillis());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
