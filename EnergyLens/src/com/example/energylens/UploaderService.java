package com.example.energylens;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	String [] file={"accelerometer_log.csv","audio_log.csv","light_log.csv","mag_log.csv","rawaudio_log.csv","wifi_log.csv",
			"Training_accelerometer_log.csv","Training_audio_log.csv","Training_light_log.csv","Training_mag_log.csv","Training_rawaudio_log.csv","Training_wifi_log.csv"};
	//	String [] file={"audio_log.csv"};
	String urlServer = Common.SERVER_URL+Common.API;
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary =  "*****";

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
		Common.changeServerUrl(sharedPref.getString("SERVER_URL", "http://192.168.20.217:9010/"));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {  
		Log.v("ELSERVICES","Uploader started "+System.currentTimeMillis());
		getUpdatedPreferences();
		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		thr.start();

		return START_NOT_STICKY; //makes sure services don't restart on stopping

	}

	Runnable mTask = new Runnable() {
		public void run() {

			synchronized (mBinder) {
				try {

					for(String filename:file){
						upload_setup(filename);
					}
					Log.i("ELSERVICES", "all files visited "+System.currentTimeMillis());
					stopSelf();
					upload_pending();
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
			if(src.getAbsolutePath().equals(path+"wifi_log.csv")){
				BufferedReader br = new BufferedReader(new FileReader(src.getAbsolutePath()));     
				if (!br.readLine().equals("time"+","+"mac"+","+"ssid"+","+"rssi"+","+"label")) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(src, true));
					bw.append("time"+","+"mac"+","+"ssid"+","+"rssi"+","+"label");
					bw.newLine();
					bw.close();
				}
			}
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
		SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy_hh-mm-ss");
		//		   	 String unique= date.toString().replace(" ", "");
		//		   	 Log.i("ELSERVICES","path new: "+unique.replace(":",""));
		return ft.format(date);
	}

	public void upload_pending(){

		File list[] = (new File(path)).listFiles();

		Arrays.sort(list, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			} });

		//
		for(int i=list.length-1;i>=0;i--){
			File file=list[i];
			String filename=file.getAbsolutePath().replace(path, "");
			Log.i("ELSERVICES", "pending "+filename);
			if(filename.contains("upload_")){
				Log.i("ELSERVICES", "uploading pending files");
				if(file.length()==0)
					file.delete();
				else
					upload(file);
			}
		}
	}

	public void upload_setup(String filename){
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String devId=telephonyManager.getDeviceId();
		String pathToFile=path+filename;
		String[] split=filename.split("\\.");
		String uniqueID=getDate();
		String upPathToFile=path+devId+'_'+"upload_"+split[0]+"_"+uniqueID+".csv"; 

		File oldFile=new File(pathToFile);
		File upFile=new File(upPathToFile);

		if(oldFile.exists()){
			fileCopy(oldFile,upFile);
			if(upFile.length()==0)
				upFile.delete();
			else
				upload(upFile);
		}
	}


	public void upload(File upFile){
		try
		{
			Log.i("ELSERVICES", Common.SERVER_URL+Common.API);
			URL url = new URL("http://198.162.20.217:9010/");
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
				Log.v("ELSERVICES", "type: "+serverResponseType+'\n'+"code: "+serverResponseCode+'\n'+"message+: "+serverResponseMessage);

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
