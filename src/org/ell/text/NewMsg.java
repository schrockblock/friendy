package org.ell.text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewMsg extends Activity implements OnClickListener, TextWatcher{
	EditText et;
	Button b;
	Person p;
	FriendyApp fa;
	ContentResolver tcr;
	Vector<Person> people;
	public static final String SMS_ADDRESS_PARAM="SMS_ADDRESS_PARAM";
	public static final String SMS_DELIVERY_MSG_PARAM="SMS_DELIVERY_MSG_PARAM";
	public static final String SMS_SENT_ACTION="com.tilab.msn.SMS_SENT";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.newmsg);
        
        fa = (FriendyApp)getApplicationContext();
        people=fa.getPpl();
        
        Bundle ext = getIntent().getExtras();
        String i = ext.getString("id");
        
    	String body = ext.getString("body");
    	//long date = ext.getLong("date");
    	if (fa.findIndex(i)!=-1){
    		if (people.get(fa.findIndex(i)).getMsgs().size()==0){
    			
    		}else{
	    		people.get(fa.findIndex(i)).insertMsg(new MsgHolder(Long.toString(System.currentTimeMillis()),body));
	        	fa.setPpl(people);
	        	p=people.get(fa.findIndex(i));
	        	people.remove(p);
	        	people.add(0, p);
	        	fa.setPpl(people);
    		}
    	}else{
    		p=new Person(i);
    		p.addMsg(new MsgHolder(Long.toString(System.currentTimeMillis()),body));
	    	String[] proj = new String[] {PhoneLookup.DISPLAY_NAME,PhoneLookup.PHOTO_ID};
    		Uri u =Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(p.n));
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
	        		}
	        		photo.close();
        		}catch(Exception e){
        			
        		}
        	}
    		people.add(0,p);
    		fa.setPpl(people);
    	}
    	
        
    	TextView msgbody=(TextView)findViewById(R.id.newmsg);
    	TextView msgdate=(TextView)findViewById(R.id.newdate1);
    	ImageView image = (ImageView)findViewById(R.id.iv1);
    	et = (EditText)findViewById(R.id.edit1);
    	b = (Button)findViewById(R.id.send1);
    	b.setOnClickListener(this);
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa MM/dd/yy");
    	
    	msgbody.setText(body);
    	Linkify.addLinks(msgbody, Linkify.ALL);
    	msgdate.setText(sdf.format(new Date(System.currentTimeMillis())));
    	
    	if (p.bmp!=null){
			image.setImageBitmap(p.bmp);
    	}
    	
    	if (p.dName!=null){
    		setTitle(p.dName);
    	}else{
    		setTitle(p.n);
    	}
    	
    	tcr=this.getContentResolver();
    	Thread thread = new Thread(new Runnable(){
        	public void run(){
        		Vector<MsgHolder> m1 = p.getMsgs();
        		Uri inbox = Uri.parse("content://sms/inbox");
    			String date = m1.get(0).d;
    			Cursor c = managedQuery(inbox,new String[] {"address","date"},"address='"+p.n+"' and date='"+date+"'",null,null);
    			if (c.moveToFirst()){
        			ContentValues cv = new ContentValues();
        			cv.put("read", true);
        			tcr.update(inbox, cv, "address='"+p.n+"' and date='"+date+"'", null);
    			}
        	}
        });
        thread.start();
    }

	@Override
	public void onClick(View v) {
		if (v.getId()==b.getId()){
			String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
			
			SmsManager smsMgr = SmsManager.getDefault();
			
			String smsText = et.getText().toString();
			ArrayList<String> messages = smsMgr.divideMessage(smsText);
			Intent sentIntent = new Intent(SMS_SENT_ACTION);
	        sentIntent.putExtra(SMS_ADDRESS_PARAM, p.n);
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
			smsMgr.sendMultipartTextMessage(p.n, null, messages, listOfIntents, null);
			String millis = Long.toString(System.currentTimeMillis());
			ContentValues values = new ContentValues();
			values.put("address", p.n);
			values.put("body", smsText);
			values.put("date", millis);
			this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
			Vector<MsgHolder> m2=people.get(fa.findIndex(p.n)).getSentMsgs();
			if (m2.size()>0){
				m2.add(0, new MsgHolder(millis,smsText));
				people.get(fa.findIndex(p.n)).setSentMsgs(m2);
				fa.setPpl(people);
			}
			p=people.get(fa.findIndex(p.n));
	    	people.remove(p);
	    	people.add(0, p);
	    	fa.setPpl(people);
		}
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancelAll();
		finish();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancelAll();
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length()!=0){
			b.setEnabled(true);
		}else{
			b.setEnabled(false);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length()!=0){
			b.setEnabled(true);
		}else{
			b.setEnabled(false);
		}
	}

}
