package org.ell.text;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class FriendyMain extends Activity implements OnClickListener{
	Context context;
	Vector<Person> people;
	Facebook facebook = new Facebook("175828775851873");
	FriendsGetProfilePics fgpp = new FriendsGetProfilePics();
	JSONArray jArray=null;
	Vector<HashMap<String,Object>> friendlist=new Vector<HashMap<String,Object>>();
	String FILENAME = "AndroidSSO_data";
    private SharedPreferences mPrefs;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setImage(msg.what);
        	setName(msg.what);
        }
    };
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	setFbImage(msg.what);
        }
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
        /*
         * Get existing access_token if any
         */
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {}, new DialogListener() {
                @Override
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                }
    
                @Override
                public void onFacebookError(FacebookError error) {}
    
                @Override
                public void onError(DialogError e) {}
    
                @Override
                public void onCancel() {}
            });
        }
        
//        AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
//        mAsyncRunner.request("me/friends", new FriendsRequestListener());
        
        load();
    }
    
    private void load(){
        Log.d("Start query",""+(Calendar.getInstance()).getTimeInMillis());
        Uri inbox = Uri.parse("content://sms/inbox");
        CursorLoader cl = new CursorLoader(this, inbox,null,null,null,null);
        Cursor c = cl.loadInBackground();
        Log.d("End query", ""+(Calendar.getInstance()).getTimeInMillis());
        
        people= new Vector<Person>();
        if(c.moveToFirst()){
        	Log.d("Start sort",""+(Calendar.getInstance()).getTimeInMillis());
            do{
                    String body= c.getString(c.getColumnIndexOrThrow("body")).toString();
                    String number=c.getString(c.getColumnIndexOrThrow("address")).toString();
                    //String person=c.getString(c.getColumnIndexOrThrow("person")).toString();
                    String date=c.getString(c.getColumnIndexOrThrow("date")).toString();
                    String read=c.getString(c.getColumnIndexOrThrow("read")).toString();
                    boolean unread=read.equals("0");
                    MsgHolder m = new MsgHolder(date, body);
                    m.unread=unread;
                    Person p = new Person(number);
                    if (people.contains(p)){
                    	int i = people.indexOf(p);
                    	Person p1 = people.get(i);
                    	p1.addMsg(m);
                    	if (unread&&!p1.unread){
                        	p1.unread=unread;
                        }
                    	people.set(i, p1);
                    }else{
                    	if (unread){
                        	p.unread=unread;
                        }
                    	p.addMsg(m);
                    	people.add(p);
                    }
            }while(c.moveToNext());
            Log.d("End sort",""+(Calendar.getInstance()).getTimeInMillis());
        }
        c.close();
        
        Thread fbFriendsThread = new Thread(new Runnable(){
			@Override
			public void run() {
				Log.d("Start fb list",""+(Calendar.getInstance()).getTimeInMillis());
				try {
		            //String query = "select name, uid, pic_square from user where uid in (select uid2 from friend where uid1=me()) order by name";
		            Bundle params = new Bundle();
		            params.putString("fields", "name, picture, location");
		            //params.putString("query", query);
				    JSONObject response = Util.parseJson(facebook.request(
				    "me/friends", params, "GET"));
				    
				    jArray = response.getJSONArray("data");

				    for (int j = 0; j < jArray.length(); j++) {
				        JSONObject json_data = jArray.getJSONObject(j);
				        HashMap<String,Object> tempMap = new HashMap<String, Object>();
//				        String name=json_data.getString("name");
//				        String id=json_data.getString("id");
				        tempMap.put("friend", json_data.getString("name"));
				        tempMap.put("id", json_data.getString("id"));
				        friendlist.add(tempMap);
				    }
				    
				    //check people and images
			        Log.d("End fb list",""+(Calendar.getInstance()).getTimeInMillis());
				} catch (Exception e) {
				    e.printStackTrace();
				} catch (FacebookError e3){
					e3.printStackTrace();
				}
			}
        });
        fbFriendsThread.start();

//        Thread infoThread = new Thread(new Runnable(){
//			@Override
//			public void run() {
				Log.d("Start info loop",""+(Calendar.getInstance()).getTimeInMillis());
		    	String[] proj = new String[] {PhoneLookup.DISPLAY_NAME,PhoneLookup.PHOTO_ID};
		        for (int i=0;i<people.size();i++){
		        	//String num = people.get(i).n;
		        	Uri u =Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(people.get(i).n));
		        	Cursor phones;
		        	CursorLoader cl2 = new CursorLoader(context,u, proj, null, null,null);
		        	phones = cl2.loadInBackground();
		        	if (phones.moveToFirst()){
		        		String name = phones.getString(phones.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		        		people.get(i).dName=name;
		        		if (name.contains("Quinn")){
		        			Log.d("Quinn","");
		        		}
		        		handler.sendEmptyMessage(i);
		        		Cursor photo;
		        		try{
			        		String photoId=phones.getString(phones.getColumnIndex(PhoneLookup.PHOTO_ID));
			        		people.get(i).picId=photoId;
			        		cl2 = new CursorLoader(context, Data.CONTENT_URI,new String[] {Photo.PHOTO},Data._ID + "=?", new String[]{photoId},null);
			        		photo = cl2.loadInBackground();
			        		if(photo.moveToFirst()) {
			        			byte[] photoBlob = photo.getBlob(photo.getColumnIndex(Photo.PHOTO));
			        			Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
			        			people.get(i).bmp=photoBitmap;
			        		}
			        		photo.close();
		        		}catch(Exception e){
		        			Log.d("No picture record",people.get(i).dName);
		        			//photo.close();
		        		}
		        	}else{
		        		Log.d("No record",people.get(i).n+":"+people.get(i).dName);
		        	}
		        	phones.close();
		        }
		        Log.d("End info loop",""+(Calendar.getInstance()).getTimeInMillis());
		        checkImages();
//			}
//        });
//        infoThread.start();
        
        FriendyApp fa = (FriendyApp)getApplicationContext();
        fa.setPpl(people);
    }

    private ImageButton defaultIb(Context context){
    	ImageButton iB= new ImageButton(context);
		iB.setAdjustViewBounds(true);
		float scale = context.getResources().getDisplayMetrics().density;
		iB.setMaxHeight((int)(100*scale));
		iB.setMaxWidth((int)(100*scale));
		iB.setBackgroundColor(Color.rgb(8,45,76));
		return iB;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
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
	        onResume();
	        return true;
	    case R.id.newmsg:
	        Intent i = new Intent(this, SendNew.class);
	        startActivity(i);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    @Override
    public void onResume() {
    	super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
        
        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setBackgroundColor(Color.rgb(8,45,76));
        ll.setOrientation(LinearLayout.VERTICAL);
        TableLayout tl = new TableLayout(this);
        tl.setBackgroundColor(Color.rgb(8,45,76));
//        tl.setLayoutParams(new TableLayout.LayoutParams(
//                    LayoutParams.FILL_PARENT,
//                    LayoutParams.WRAP_CONTENT));
        tl.setOrientation(TableLayout.VERTICAL);
        tl.setGravity(Gravity.CENTER_VERTICAL);
        
        TableRow tr=new TableRow(this);
        tr.setGravity(Gravity.CENTER);
        TableRow tr2=new TableRow(this);
        tr2.setGravity(Gravity.CENTER);
        
        for (int i=0;i<people.size();i++){
        	boolean unread = people.get(i).unread;
        	ImageButton iB= defaultIb(this);
			Drawable d = getResources().getDrawable(R.drawable.android);
			iB.setImageDrawable(d);
			iB.setId(i);
			iB.setOnClickListener(this);
			
			Person p = people.get(i);
			
			
			
			String name;
			if ((name=p.dName)==null){
				name=p.n;
			}
			
			if (i%2==0){
				tr=new TableRow(this);
				tr.setGravity(Gravity.CENTER);
				tr2=new TableRow(this);
				tr2.setGravity(Gravity.CENTER);
				tr.addView(iB);
				TextView tv = new TextView(this);
				tv.setTypeface(Typeface.DEFAULT_BOLD);
				tv.setText(name);
				tv.setGravity(Gravity.CENTER);
				tv.setBackgroundColor(Color.rgb(8,45,76));
				tv.setId(20000+i);
				if(unread){
					tv.setTextColor(Color.rgb(0,0,250));
				}
				tr2.addView(tv);
			}else if (i<=people.size()-1){
				tr.addView(iB);
				TextView tv = new TextView(this);
				tv.setTypeface(Typeface.DEFAULT_BOLD);
				tv.setText(p.dName != null ? p.dName : p.n);
				if(unread){
					tv.setTextColor(Color.rgb(0,0,250));
				}
				tv.setGravity(Gravity.CENTER);
				tv.setBackgroundColor(Color.rgb(8,45,76));
				tv.setId(20000+i);
				tr2.addView(tv);
				if (tr.getParent()==null){
					tl.addView(tr);
					tl.addView(tr2);
				}
			}
			if (i==people.size()-1){
				if (tr.getParent()==null){
					tl.addView(tr);
					tl.addView(tr2);
				}
			}
        }
        
        //ll.addView(tl);
        sv.addView(tl);
        setContentView(sv);
        
        checkImages();
    }
    
    private void checkImages(){
    	Thread checker = new Thread(new Runnable(){
			@Override
			public void run() {
				for (int i=0;i<people.size();i++){
					Person p = people.get(i);
					
					if (p.bmp!=null){
						handler.sendEmptyMessage(i);
						//iB.setImageBitmap(p.bmp);
					}else if (p.picId!=null){
							if (p.picId.equals("")){
								Cursor photo = managedQuery(Data.CONTENT_URI,
														new String[] {Photo.PHOTO},Data._ID + "=?", 
														new String[]{p.picId},null);
				        		if(photo.moveToFirst()) {
				        			byte[] photoBlob = photo.getBlob(photo.getColumnIndex(Photo.PHOTO));
				        			Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
				        			people.get(i).bmp=photoBitmap;
				        			handler.sendEmptyMessage(i);
				        			//iB.setImageBitmap(photoBitmap);
				        		}else{
				        			File imgFile = new  File(p.picId);
								if(imgFile.exists()){
								    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
								    people.get(i).bmp=myBitmap;
								    handler.sendEmptyMessage(i);
								    //ib.setImageBitmap(myBitmap);
								}
			        		}
				        		photo.close();
						}
					}else{
						//Facebook that shit, or do nothing
		        		for (int k=0;k<friendlist.size();k++){
		        			if (people.get(i).dName!=null){
			        			String fbname=(String)friendlist.get(k).get("friend");
			        			String[] cname=people.get(i).dName.split(" ");
			        			
			        			if (fbname.contains(cname[0])&&fbname.contains(cname[cname.length-1])&&cname.length!=1){
			        				JSONObject jsonObject = null;
			        	            try {
			        	                jsonObject = jArray.getJSONObject(k);
			        	            } catch (JSONException e1) {
			        	                e1.printStackTrace();
			        	            }
			        	    		try {
			        	    			people.get(i).fbid=jsonObject.getString("id");
			        	    			people.get(i).fbpos=k;
			        	    			handler2.sendEmptyMessage(i);
			        	    			break;
			        				} catch (JSONException e) {
			        					e.printStackTrace();
			        				}
	//		        				try{
	//		        					Bitmap fbimg=fgpp.getImage((String)friendlist.get(k).get("id"),jArray.getJSONObject(k).getString("picture"));
	//		        					if (fbimg!=null){
	//		        						people.get(i).bmp=fbimg;
	//		        					}
	//		        				}catch(Exception e2){
	//		        					e2.printStackTrace();
	//		        				}
			        			}
		        			}
		        		}
					}
				}
				FriendyApp fa = (FriendyApp)getApplicationContext();
		        fa.setPpl(people);
			}
    	});
    	checker.start();
    }
    
    public void notifyChange(){
    	setFbPics();
    }
    
    private void setFbPics(){
    	for (int i=0;i<people.size();i++){
    		Person per = people.get(i);
    		if (per.fbid!=null){
    			Bitmap b = Utility.model.getImage(per.fbid);
    			if (b!=null){
    				people.get(i).bmp=b;
    				handler.sendEmptyMessage(i);
    			}
    		}
    	}
    }
    
    private void setFbImage(int i){
    	if (Utility.model == null) {
            Utility.model = new FriendsGetProfilePics();
            Utility.model.setListener(this);
        }
		JSONObject jsonObject = null;
        try {
            jsonObject = jArray.getJSONObject(people.get(i).fbpos);
			people.get(i).bmp = Utility.model.getImage(
			        jsonObject.getString("id"), jsonObject.getString("picture"));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
    
    private void setImage(int i){
    	ImageButton ib = (ImageButton)findViewById(i);
    	
    	if (people.get(i).bmp!=null){
    		ib.setImageBitmap(people.get(i).bmp);
    	}
    }
    
    private void setName(int i){
    	TextView tv = (TextView)findViewById(20000+i);
    	
    	tv.setText(people.get(i).dName);
    }

	@Override
	public void onClick(View v) {
		Intent i = new Intent(this,Msgs.class);
		i.putExtra("id", people.get(v.getId()).n);
		i.putExtra("new", false);
		startActivity(i);
	}
}