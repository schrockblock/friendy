package org.ell.text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SendNew extends Activity implements OnClickListener {
	AutoCompleteTextView auto;
	EditText et;
	boolean pressed = false;
	boolean delivered=false;
	public static final String SMS_ADDRESS_PARAM="SMS_ADDRESS_PARAM";
	public static final String SMS_SENT_ACTION="com.tilab.msn.SMS_SENT";
	public static final String SMS_DELIVERY_MSG_PARAM="SMS_DELIVERY_MSG_PARAM";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendnew);
		
		//et = (EditText)findViewById(R.id.editText1);
        auto = (AutoCompleteTextView) findViewById(R.id.auto_com);
        et = (EditText)findViewById(R.id.eT2);
		
		Cursor c = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone._ID},
				null,null,null);
		//boolean q = c.moveToFirst();
		ContactsAutoCompleteCursorAdapter adapter = new ContactsAutoCompleteCursorAdapter(this, c);
		auto.setAdapter(adapter);
		Button b = (Button)findViewById(R.id.send);
		b.setTag("");
		b.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		FriendyApp fa = (FriendyApp)getApplicationContext();;
		Vector<Person> people=fa.getPpl();
		Person p;
		
		String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
		
		SmsManager smsMgr = SmsManager.getDefault();
		
		String number = auto.getEditableText().toString();
		p=new Person(number);
		if (people.contains(p)){
			p=fa.findNum(number);
		}else{
			String[] proj = new String[] {PhoneLookup.DISPLAY_NAME,PhoneLookup.PHOTO_ID};
			Uri u =Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        	Cursor phones = managedQuery(u, proj, null, null,null);
        	if (phones.moveToFirst()){
        		String name = phones.getString(phones.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        		p.dName=name;
        		Cursor photo;
        		try{
	        		String photoId=phones.getString(phones.getColumnIndex(PhoneLookup.PHOTO_ID));
	        		p.picId=photoId;
	        		photo = managedQuery(Data.CONTENT_URI,new String[] {Photo.PHOTO},Data._ID + "=?", new String[]{photoId},null);
	        		if(photo.moveToFirst()) {
	        			byte[] photoBlob = photo.getBlob(photo.getColumnIndex(Photo.PHOTO));
	        			Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
	        			p.bmp=photoBitmap;
	        		}else{
	        			
	        		}
	        		photo.close();
        		}catch(Exception e){
        			
        		}
        	}
        	fa.people.add(p);
		}
		String smsText = et.getText().toString();
		ArrayList<String> messages = smsMgr.divideMessage(smsText);
		Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra(SMS_ADDRESS_PARAM, number);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, sentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        delivered=true;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        delivered=true;
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));  
        ArrayList<PendingIntent> listOfIntents = new ArrayList<PendingIntent>();
        listOfIntents.add(pi);
        listOfIntents.add(sentPI);
        listOfIntents.add(deliveredPI);
		smsMgr.sendMultipartTextMessage(number, null, messages, listOfIntents, null);
		if (true){
			String millis = Long.toString(System.currentTimeMillis());
			ContentValues values = new ContentValues();
			values.put("address", number);
			values.put("body", smsText);
			values.put("date", millis);
			this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
			
			Vector<MsgHolder> m2=people.get(fa.findIndex(p.n)).getSentMsgs();
			m2.add(0, new MsgHolder(millis,smsText));
			people.get(fa.findIndex(p.n)).setSentMsgs(m2);
			fa.setPpl(people);
			p=people.get(fa.findIndex(p.n));
	    	people.remove(p);
	    	people.add(0, p);
	    	fa.setPpl(people);
		}
		finish();
	}
}
