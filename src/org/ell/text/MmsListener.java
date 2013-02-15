package org.ell.text;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MmsListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")){
            Bundle bundle = intent.getExtras();
            try{
                if (bundle != null){
                    //Retrieve MMS---
                    String type = intent.getType();
                    if(type.trim().equalsIgnoreCase("application/vnd.wap.mms-message")){
                        byte[] buffer = bundle.getByteArray("data");
                        String incomingNumber = new String(buffer);
                        int indx = incomingNumber.indexOf("/TYPE");
                        if(indx>0 && (indx-15)>0){
                            int newIndx = indx - 15;
                            incomingNumber = incomingNumber.substring(newIndx, indx);
                            indx = incomingNumber.indexOf("+");
                            if(indx>0){
                                incomingNumber = incomingNumber.substring(indx);
                                Notification notif = new Notification(
                    					R.drawable.icon, // the icon for the status bar
                    					"MMS from: "+incomingNumber,
                    					System.currentTimeMillis());
                                CharSequence contentTitle = "Friendy";
                                CharSequence contentText = "New MMS";
                                Intent notificationIntent = new Intent(context, Msgs.class);
                                notificationIntent.putExtra("id", incomingNumber);
                                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                    	        notif.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
                                notif.defaults |= Notification.DEFAULT_SOUND;
                    	        notif.defaults |= Notification.DEFAULT_VIBRATE;
                    	        notif.flags |= Notification.FLAG_AUTO_CANCEL;
                    	        nm.notify(1, notif);
                            }
                        }
                    }
                }
            }catch(Exception e){
                       Log.d("MMS data Exception caught", e.getMessage());
            }
        }
	}

}
