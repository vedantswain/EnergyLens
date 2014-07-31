package com.example.energylens;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class UploaderService extends Service{
	
	HttpURLConnection connection = null;
	DataOutputStream outputStream = null;
	DataInputStream inputStream = null;
	String path = Environment.getExternalStorageDirectory()+File.separator+"EnergyLens+"+File.separator;
	String [] file={"accelerometer_log.csv","audio_log.csv","light_log.csv","mag_log.csv","rawaudio_log.csv","wifi_log.csv",
			"Training_accelerometer_log.csv","Training_audio_log.csv","Training_light_log.csv","Training_mag_log.csv","Training_rawaudio_log.csv","Training_wifi_log.csv"};
	String urlServer = "http://192.168.20.217:9010/data/upload/";
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

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {  
	    	Log.v("ELSERVICES","Uploader started "+System.currentTimeMillis());
	    	Thread thr = new Thread(null, mTask, "AlarmService_Service");
	        thr.start();
	       
	        return START_NOT_STICKY; //makes sure services don't restart on stopping
	       
	    }
	    
	    Runnable mTask = new Runnable() {
	        public void run() {
	        	
	            synchronized (mBinder) {
	              try {
	            	for(String filename:file){
	            		upload(filename);
	            		}
	            	Log.i("ELSERVICES", "all files visited "+System.currentTimeMillis());
	            	stopSelf();
	               	  }

	               catch (Exception e) {
	              }
	            }
	        }
	      };
	    
	   public void fileCopy(File src,File dst){
		   if(!dst.exists()){
			   try {
				dst.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	   }
	      
	    public void upload(String filename){
	    	try
	    	{
	    		int upload_flag=1;
	    	    URL url = new URL(urlServer);
	    	    connection = (HttpURLConnection) url.openConnection();
//	    	    connection.setChunkedStreamingMode(0);
	    	    
	    	 // Allow Inputs &amp; Outputs.
	    	    connection.setDoInput(true);
	    	    connection.setDoOutput(true);
	    	    connection.setUseCaches(false);
	    	 
	    	    // Set HTTP method to POST.
	    	    connection.setRequestMethod("POST");
	    	    	    		    	    
	    		String pathToFile=path+filename;
	    		String upPathToFile=path+"upload_"+filename;
	    	
	    		File oldFile=new File(pathToFile);
	    		File upFile=new File(upPathToFile);
	    		
	    		if(oldFile.exists() && !upFile.exists()){
	    			fileCopy(oldFile,upFile);
	    		}
	    		else if(!oldFile.exists() || !upFile.exists()){
	    			Log.i("ELSERVICES", "No new log to upload");
	    			upload_flag=0;
	    		}
	    		
	    		Log.i("ELSERVICES","Response code received "+connection.getResponseMessage() );
	    		
	    		if(!(connection.getResponseCode()>=200 && connection.getResponseCode()<300)){
	    			upload_flag=0;
	    			Log.i("ELSERVICES","Files not uploaded because: "+connection.getResponseMessage() );
	    		}
	    		
	    		if(upload_flag==1){
		    	    Log.v("ELSERVICES",upFile.getAbsolutePath());
		    	 
		    		
		    	    FileInputStream fileInputStream = new FileInputStream(upFile);
			    	Log.i("ELSERVICES", upFile.getAbsolutePath());
			    	    
			    	    if(connection!=null){
					    	 
				    	    
				    	 
				    	    connection.setRequestProperty("Connection", "Keep-Alive");
				    	    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
				    	 
				    	    outputStream = new DataOutputStream( connection.getOutputStream() );
				    	    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				    	    outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" +"upload_"+pathToFile +"\"" + lineEnd);
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
				    	    upFile.delete();
	
					    	Log.v("ELSERVICES", "Upload complete "+System.currentTimeMillis());
			    	    }
			    	    else{
			    	    	Log.v("ELSERVICES", "Upload failed "+System.currentTimeMillis());
			    	    }
		    	    
	    		}  
	    	}
	    	catch (Exception e)
	    	{
	    	    e.printStackTrace();
	    	}
	    	
	    }
}
