package org.ell.text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Msgs extends Activity implements OnClickListener, TextWatcher{
	EditText et;
	Button b;
	Person p;
	ListView lv;
	FriendyApp fa;
	ContentResolver tcr;
    Vector<Person> people;
    boolean delivered=false;
	public static final String SMS_ADDRESS_PARAM="SMS_ADDRESS_PARAM";
	public static final String SMS_DELIVERY_MSG_PARAM="SMS_DELIVERY_MSG_PARAM";
	public static final String SMS_SENT_ACTION="com.tilab.msn.SMS_SENT";
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			lv.post(new Runnable(){
          	  public void run() {
          		    lv.setSelection(lv.getCount() - 1);
          	  }});
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msgs);
        //setContentView(R.layout.main);
    }
    
    private void load(){
        	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            
            lv = (ListView)findViewById(R.id.msg_list);
            
            et = (EditText)findViewById(R.id.eT1);
            et.setOnClickListener(this);
            et.addTextChangedListener(this);
            b=(Button)findViewById(R.id.send);
            b.setOnClickListener(this);
            b.setEnabled(false);
            
            Bundle ext = getIntent().getExtras();
            String i = ext.getString("id");
            
            fa = (FriendyApp)getApplicationContext();
            people=fa.getPpl();
            
            p = people.get(fa.findIndex(i));
            if (p.unread){
            	tcr=this.getContentResolver();
            	Thread thread = new Thread(new Runnable(){
                	public void run(){
                		Vector<MsgHolder> m1 = p.getMsgs();
                		Uri inbox = Uri.parse("content://sms/inbox");
                		for (int j=0;j<m1.size();j++){
                			if (m1.get(j).unread){
    	            			String body = m1.get(j).b;
    	            			String date = m1.get(j).d;
    	            			Cursor c = managedQuery(inbox,new String[] {"address","date"},"address='"+p.n+"' and date='"+date+"'",null,null);
    	            			if (c.moveToFirst()){
    	                			ContentValues cv = new ContentValues();
    	                			cv.put("read", true);
    	                			tcr.update(inbox, cv, "address='"+p.n+"' and date='"+date+"'", null);
    	            			}
    	            			c.close();
                			}
                		}
                	}
                });
                thread.start();
            }
            people.get(fa.findIndex(i)).unread=false;
            p = people.get(fa.findIndex(i));
            Vector<MsgHolder> m1 = p.getMsgs();
            Vector<MsgHolder> m2 = p.getSentMsgs();
            
            if (p.dName!=null){
            	setTitle(p.dName);
            }else{
            	setTitle(p.n);
            }
            
            if (m2.size()==0){
    	        Uri sent = Uri.parse("content://sms/sent");
    	        Cursor c = managedQuery(sent,null,null,null,null);
    	        
    	        if(c.moveToFirst()){
    	            do{
    	                    String body= c.getString(c.getColumnIndexOrThrow("body")).toString();
    	                    String number=c.getString(c.getColumnIndexOrThrow("address")).toString();
    	                    String date=c.getString(c.getColumnIndexOrThrow("date")).toString();
    	                    MsgHolder mh = new MsgHolder(date, body);
    	                    if (number.equals(p.n)&&!p.getSentMsgs().contains(mh)){
    	                    	m2.add(mh);
    	                    }
    	            }while(c.moveToNext());
    	        }
    	        c.close();
    	        people.get(fa.findIndex(i)).setSentMsgs(m2);
    	        p = people.get(fa.findIndex(i));
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa MM/dd/yy");
            
            //setup adapter
            ConvoAdapter ca = new ConvoAdapter(this, m1, m2);
            lv.setAdapter(ca);
            //scroll to bottom
            lv.post(new Runnable(){
            	  public void run() {
            		    lv.setSelection(lv.getCount() - 1);
            	  }});
            
            Uri draft = Uri.parse("content://sms/draft");
            Cursor c = managedQuery(draft,null,null,null,null);
            
            if (c.moveToFirst()){
            		String number="";
            		do{
            			try{
            				String s = c.getString(c.getColumnIndexOrThrow("read")).toString();
            				number=c.getString(c.getColumnIndexOrThrow("address")).toString();
            			}catch(Exception e){
            				try{
            					String personColumn = c.getString(c.getColumnIndexOrThrow("person")).toString();
            				}catch(Exception e2){
            					
            				}
            			}
    	        		if (number.equals(p.n)){
    	        			String body= c.getString(c.getColumnIndexOrThrow("body")).toString();
    	        			String date= c.getString(c.getColumnIndexOrThrow("date")).toString();
    	        			et.setText(body);
    	        			b.setEnabled(true);
    	        			this.getContentResolver().delete(Uri.parse("content://sms"), "body=? and date=?", new String[] {body,date});
    	        		}
            		}while(c.moveToNext());
            }
            c.close();
    }

    @Override
    public void onPause(){
    	super.onPause();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	
    	if (et.getText().toString().length()!=0){
    		String smsText = et.getText().toString();
    		String millis = Long.toString(System.currentTimeMillis());
			ContentValues values = new ContentValues();
			values.put("address", p.n);
			values.put("body", smsText);
			values.put("date", millis);
			this.getContentResolver().insert(Uri.parse("content://sms/draft"), values);
			Toast.makeText(this, "Message saved as a draft", 5000).show();
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.refresh:
	        load();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onClick(View v) {
		if (v.getId()!=et.getId()){
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
			smsMgr.sendMultipartTextMessage(p.n, null, messages, listOfIntents, null);
			if (true){
				et.setText("");
				String millis = Long.toString(System.currentTimeMillis());
				ContentValues values = new ContentValues();
				values.put("address", p.n);
				values.put("body", smsText);
				values.put("date", millis);
				this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
				Vector<MsgHolder> m2=people.get(fa.findIndex(p.n)).getSentMsgs();
				m2.add(0, new MsgHolder(millis,smsText));
				ConvoAdapter ca = new ConvoAdapter(this, p.getMsgs(), m2);
				lv.setAdapter(ca);
				lv.invalidateViews();
				people.get(fa.findIndex(p.n)).setSentMsgs(m2);
				fa.setPpl(people);
				p=people.get(fa.findIndex(p.n));
		    	people.remove(p);
		    	people.add(0, p);
		    	fa.setPpl(people);
			}
		}
		(new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
			
		})).start();
	}

	@Override
	public void onResume(){
		super.onResume();
		
		load();
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
