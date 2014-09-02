package com.example.energylens;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TimeSelectActivity extends Activity implements TimePickerDialogFragment.TimePickerDialogListener,
DatePickerDialogFragment.DatePickerDialogListener{

	Calendar c=Calendar.getInstance();
	int changeTimeOf=0;
	int changeDateOf=0;
	int year,month,day,hourOfDay,minute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_select);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.time_select, menu);
		updateText(System.currentTimeMillis()-12*60*60*1000,System.currentTimeMillis());

		c.setTimeInMillis(System.currentTimeMillis());

		year=c.get(Calendar.YEAR);
		month=c.get(Calendar.MONTH);
		day=c.get(Calendar.DAY_OF_MONTH);
		hourOfDay=c.get(Calendar.HOUR_OF_DAY);
		minute=c.get(Calendar.MINUTE);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateText(long start_time,long end_time){
		Button fromDate=(Button) findViewById(R.id.fromDateBtn);
		Button fromTime=(Button) findViewById(R.id.fromTimeBtn);
		Button toDate=(Button) findViewById(R.id.toDateBtn);
		Button toTime=(Button) findViewById(R.id.toTimeBtn);

		Date from_init=new Date();
		from_init.setTime(start_time);

		Date to_init=new Date();
		to_init.setTime(end_time);

		SimpleDateFormat df_date=new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat df_time=new SimpleDateFormat("HH:mm");

		fromDate.setText(df_date.format(from_init));
		toDate.setText(df_date.format(to_init));
		fromTime.setText(df_time.format(from_init));
		toTime.setText(df_time.format(to_init));

		Log.v("ELSERVICES", "From: "+from_init.toString()+" To: "+to_init.toString());
	}

	@Override
	public void onSetTime(int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		this.hourOfDay=hourOfDay;
		this.minute=minute;

		c.set(this.year, this.month, this.day, this.hourOfDay, this.minute);

		if(changeTimeOf==0){
			if(c.getTimeInMillis()>Common.TIME_PERIOD_END || c.getTimeInMillis()>System.currentTimeMillis())
				Toast.makeText(getApplicationContext(), "Illegal from time", 1000);
			else
				Common.changeTimePeriodStart(c.getTimeInMillis());
		}
		else{
			if(c.getTimeInMillis()<Common.TIME_PERIOD_START || c.getTimeInMillis()>System.currentTimeMillis())
				Toast.makeText(getApplicationContext(), "Illegal to time", 1000);
			else
				Common.changeTimePeriodEnd(c.getTimeInMillis());
		}
		updateText(Common.TIME_PERIOD_START,Common.TIME_PERIOD_END);
		Common.changeTimePeriod(true);
	}



	@Override
	public void onSetDate(int year,int month,int day) {
		// TODO Auto-generated method stub
		this.year=year;
		this.month=month;
		this.day=day;

		c.set(this.year, this.month, this.day, this.hourOfDay, this.minute);

		if(changeDateOf==0){
			if(c.getTimeInMillis()>Common.TIME_PERIOD_END || c.getTimeInMillis()>System.currentTimeMillis())
				Toast.makeText(getApplicationContext(), "Illegal from time", 1000);
			else
				Common.changeTimePeriodStart(c.getTimeInMillis());
		}
		else{
			if(c.getTimeInMillis()<Common.TIME_PERIOD_START || c.getTimeInMillis()>System.currentTimeMillis())
				Toast.makeText(getApplicationContext(), "Illegal to time", 1000);
			else
				Common.changeTimePeriodEnd(c.getTimeInMillis());
		}
		updateText(Common.TIME_PERIOD_START,Common.TIME_PERIOD_END);
		Common.changeTimePeriod(true);
	}

	public void launchDatePicker(){
		DatePickerDialogFragment newFragment = new DatePickerDialogFragment();
		newFragment.show(getFragmentManager(), "Date Picker");
	}

	public void changeFromDate(View view){
		changeDateOf=0;
		launchDatePicker();
	}

	public void changeToDate(View view){
		changeDateOf=1;
		launchDatePicker();
	}

	public void launchTimePicker(){
		TimePickerDialogFragment newFragment = new TimePickerDialogFragment();
		newFragment.show(getFragmentManager(), "Time Picker");
	}

	public void changeFromTime(View view){
		changeTimeOf=0;
		launchTimePicker();
	}

	public void changeToTime(View view){
		changeTimeOf=1;
		launchTimePicker();
	}
	
	public void done(View view){
		finish();
	}
}
