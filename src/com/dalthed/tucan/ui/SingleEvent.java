package com.dalthed.tucan.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dalthed.tucan.R;
import com.dalthed.tucan.TucanMobile;
import com.dalthed.tucan.Connection.AnswerObject;
import com.dalthed.tucan.Connection.CookieManager;
import com.dalthed.tucan.Connection.RequestObject;
import com.dalthed.tucan.Connection.SimpleSecureBrowser;

public class SingleEvent extends SimpleWebListActivity {

	private CookieManager localCookieManager;
	private static final String LOG_TAG = "TuCanMobile";
	private String  URLStringtoCall;
	
	ArrayList<String> materialLink;
	
	AppointmentAdapter DateAppointmentAdapter;
	ArrayAdapter<String> FileAdapter;
	SingleEventAdapter PropertyValueAdapter;
	int mode=0;
	boolean thereAreFiles=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleevent);
		String CookieHTTPString = getIntent().getExtras().getString("Cookie");
		URLStringtoCall = getIntent().getExtras().getString("URL");
		URL URLtoCall;
		Spinner optionSpinner = (Spinner) findViewById(R.id.singleevent_spinner);
		ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,
				getResources().getStringArray(R.array.singleevent_options));
		optionSpinner.setAdapter(spinnerAdapter);
		optionSpinner.setOnItemSelectedListener(new OnItemSelectedListener());

		try {
			URLtoCall = new URL(URLStringtoCall);
			localCookieManager = new CookieManager();
			localCookieManager.generateManagerfromHTTPString(
					URLtoCall.getHost(), CookieHTTPString);
			SimpleSecureBrowser callOverviewBrowser = new SimpleSecureBrowser(
					this);
			RequestObject thisRequest = new RequestObject(URLStringtoCall,
					localCookieManager, RequestObject.METHOD_GET, "");

			callOverviewBrowser.execute(thisRequest);
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		
		
	}
	
	class SingleEventAdapter extends ArrayAdapter<String> {
		ArrayList<String> values;
		public SingleEventAdapter(ArrayList<String> properties,ArrayList<String> values) {
			super(SingleEvent.this,R.layout.singleevent_row, R.id.singleevent_row_property,
					properties);
			this.values=values;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = super.getView(position, convertView, parent);
			TextView ValueTextView = (TextView) row
					.findViewById(R.id.singleevent_row_value);
			

			ValueTextView.setText(" "+this.values.get(position));

			return row;
		}
		
	}
	
	class AppointmentAdapter extends ArrayAdapter<String> {
		ArrayList<String> appointmentTime,appointmentNumber,appointmentRoom,appointmentInstructor;
		public AppointmentAdapter(ArrayList<String> appDate,ArrayList<String> appTime
				,ArrayList<String> appNumber,ArrayList<String> appRoom,ArrayList<String> appInstructor) {
			super(SingleEvent.this,R.layout.singleevent_row_date, R.id.singleevent_row_date_date,
					appDate);
			this.appointmentTime=appTime;
			this.appointmentInstructor=appInstructor;
			this.appointmentNumber=appNumber;
			this.appointmentRoom=appRoom;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = super.getView(position, convertView, parent);
			TextView AppTimeView = (TextView) row
					.findViewById(R.id.singleevent_row_date_time);
			TextView AppNumberView = (TextView) row
					.findViewById(R.id.singleevent_row_date_number);
			TextView AppRoomView = (TextView) row
					.findViewById(R.id.singleevent_row_date_room);
			TextView AppInstructorView = (TextView) row
					.findViewById(R.id.singleevent_row_date_instructor);
			
			AppTimeView.setText(this.appointmentTime.get(position));
			if(this.appointmentNumber!=null)
				AppNumberView.setText(this.appointmentNumber.get(position));
			else
				AppNumberView.setText("");
			AppRoomView.setText(this.appointmentRoom.get(position));
			AppInstructorView.setText(this.appointmentInstructor.get(position));

			return row;
		}
		
	}
	
	
	
	@Override
	public void onPostExecute(AnswerObject result) {
		Document doc=Jsoup.parse(result.getHTML());
		String Title=doc.select("h1").text();
		
		TextView SingleEventTitle = (TextView) findViewById(R.id.singleevent_title);
		SingleEventTitle.setText(Title);
		Element rows= doc.select("table[courseid]").first().select("tr").get(1).select("td").first();
		Elements Paragraphs = rows.select("p");
		Iterator<Element> PaIt = Paragraphs.iterator();
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		
		while(PaIt.hasNext()){
			
			Element next= PaIt.next();
			String[] information = crop(next.html());
			titles.add(information[0]);
			values.add(information[1]);
			
		}
		
		PropertyValueAdapter = new SingleEventAdapter(titles, values);
		setListAdapter(PropertyValueAdapter);
		
		//Termin-Selektor:
		//Terminselektor
		
		Iterator<Element> captionIt = doc.select("caption").iterator();
		Iterator<Element> DateTable=null;
		Iterator<Element> materialTable=null;
		while(captionIt.hasNext()){
			Element next=captionIt.next();
			if(next.text().equals("Termine")){
				System.out.println(next.parent().html());
				DateTable=next.parent().select("tr").iterator();
			}
			else if(next.text().contains("Material")){
				
				materialTable=next.parent().select("tr").iterator();
			}
		}
		ArrayList<String> eventNumber = new ArrayList<String>();
		ArrayList<String> eventDate = new ArrayList<String>();
		ArrayList<String> eventTime = new ArrayList<String>();

		ArrayList<String> eventRoom = new ArrayList<String>();
		ArrayList<String> eventInstructor = new ArrayList<String>();
		
		while(DateTable.hasNext()){
			Element next=DateTable.next();
			Elements cols=next.select("td");
			eventNumber.add(cols.get(0).text());
			eventDate.add(cols.get(1).text());
			eventTime.add(cols.get(2).text() + "-" + cols.get(3).text());
			eventRoom.add(cols.get(4).text());
			eventInstructor.add(cols.get(5).text());
		}
		
		DateAppointmentAdapter = new AppointmentAdapter(eventDate, eventTime, eventNumber, eventRoom, eventInstructor);
		
		
		int ct=0;
		ArrayList<String> materialNumber = new ArrayList<String>();
		ArrayList<String> materialName = new ArrayList<String>();
		ArrayList<String> materialDesc = new ArrayList<String>();
		materialLink = new ArrayList<String>();
		ArrayList<String> materialFile = new ArrayList<String>();
		while(materialTable.hasNext()){
			Element next=materialTable.next();
			
			if(next.select("td").size()>1) {
				ct++;
				System.out.println(ct+ "  "+(ct%3));
				int mod=(ct%3);
				switch (mod) {
				case 1:
					materialNumber.add(next.select("td").get(0).text());
					materialName.add(next.select("td").get(1).text());
					
					break;
				case 2:
					materialDesc.add(next.select("td").get(1).text());
					break;
				case 0:
					materialLink.add(next.select("td").get(1).select("a").attr("href"));
					materialFile.add(next.select("td").get(1).select("a").text());
					break;
				}
			}
		}
		if(ct>2){
			FileAdapter = new AppointmentAdapter(materialNumber, materialFile, null, materialName, materialDesc);
			thereAreFiles=true;
		}			
		else 
			FileAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new String[]{"Kein Material"});
			
			
		
		
	}
	
	private static String[] crop(String startstring){
		String[] splitted = startstring.split("</b>");
		return new String[]{Jsoup.parse(splitted[0]).text().trim(),Jsoup.parse(splitted[1]).text()};
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(mode==1 && thereAreFiles){
			String url=TucanMobile.TUCAN_PROT+TucanMobile.TUCAN_HOST+materialLink.get(position);
			Log.i(LOG_TAG,url);
			Uri mUri = Uri.parse(url);
			Intent DownloadFile = new Intent(Intent.ACTION_VIEW,mUri);
			
			startActivity(DownloadFile);
		}
	}

	public class OnItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
		
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			switch (position) {
			case 0:
				setListAdapter(PropertyValueAdapter);
				mode=0;
				break;
			case 1:
				setListAdapter(FileAdapter);
				mode=1;
				break;
			case 2:
				setListAdapter(DateAppointmentAdapter);
				mode=0;
				break;
			default:
				break;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
	}
}