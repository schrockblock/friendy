package org.ell.text;

import android.content.BroadcastReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceive extends BroadcastReceiver {
	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (intent.getAction().equals(ACTION)) {
        	Bundle bundle = intent.getExtras();

        	if (bundle != null) {
        	        Object[] pdusObj = (Object[]) bundle.get("pdus");
        	        SmsMessage[] messages = new SmsMessage[pdusObj.length];

        	        // getting SMS information from Pdu.
        	        for (int i = 0; i < pdusObj.length; i++) {
        	                messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
        	        }

        	        Intent i = new Intent(context, NewMsg.class); 
        	        i.putExtra("id", messages[0].getDisplayOriginatingAddress());
        	        i.putExtra("new", true);
        	        i.putExtra("body", messages[0].getDisplayMessageBody());
        	        i.putExtra("date", (messages[0].getTimestampMillis()/*+4*3600*1000*/));
        	        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	        Notification notif = new Notification(
        					R.drawable.icon, // the icon for the status bar
        					"New Message!",
        					System.currentTimeMillis()); // intent that shows the inbox when you click on icon
        	        CharSequence contentTitle = "Friendy";
        	        CharSequence contentText = "Someone sent you a message!";
        	        Intent notificationIntent = new Intent(context, Msgs.class);
        	        notificationIntent.putExtra("id", messages[0].getDisplayOriginatingAddress());
        	        notificationIntent.putExtra("new", true);
        	        notificationIntent.putExtra("body", messages[0].getDisplayMessageBody());
        	        notificationIntent.putExtra("date", System.currentTimeMillis());//(messages[0].getTimestampMillis()/*+4*3600*1000*/));
        	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        	        notif.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        	        notif.defaults |= Notification.DEFAULT_SOUND;
        	        notif.defaults |= Notification.DEFAULT_VIBRATE;
        	        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        	        nm.notify(1, notif);
        	        context.startActivity(i);
//        	        for (SmsMessage currentMessage : messages) {
//        	        	
//        	                // currentMessage.getDisplayOriginatingAddress()   has sender's phone number
//        	                // currentMessage.getDisplayMessageBody()     has the actual message
//        	        }
        	}
        }
	}

}
