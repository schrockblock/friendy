package org.ell.text;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ConvoAdapter extends BaseAdapter {
	Context context;
	Vector<MsgHolder> rcvd = new Vector<MsgHolder>();
	Vector<MsgHolder> sent = new Vector<MsgHolder>();
	Vector<MsgHolder> all = new Vector<MsgHolder>();
	
	public ConvoAdapter(Context ctxt, Vector<MsgHolder> m1, Vector<MsgHolder> m2){
		context = ctxt;
		rcvd = m1;
		sent = m2;
		mergeMsgs();
	}

	@Override
	public int getCount() {
		return all.size();
	}

	@Override
	public Object getItem(int position) {
		return all.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MsgHolder mh = (MsgHolder)getItem(position);
		
		MsgView holder;
		
		if (convertView == null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item, parent, false);
			
			holder = new MsgView();
			holder.msg = (TextView)convertView.findViewById(R.id.msg_txt);
			holder.date = (TextView)convertView.findViewById(R.id.msg_time);
			holder.rl = (RelativeLayout)convertView.findViewById(R.id.rl);
			
			convertView.setTag(holder);
		}else{
			holder = (MsgView)convertView.getTag();
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa MM/dd/yy");
		
		holder.msg.setText(mh.b);
		Linkify.addLinks(holder.msg, Linkify.ALL);
		holder.date.setText(sdf.format(new Date(Long.parseLong(mh.d))));
		
		int bgColor = Color.WHITE;
		int txtColor = Color.BLACK;
		if (mh.sender.equals("me")){
			txtColor = Color.WHITE;
			bgColor = Color.rgb(8,45,76);
		}else{
			txtColor = Color.rgb(8,45,76);
			bgColor = Color.WHITE;
		}
		
		holder.msg.setBackgroundColor(bgColor);
		holder.date.setBackgroundColor(bgColor);
		holder.msg.setTextColor(txtColor);
		holder.date.setTextColor(txtColor);
		holder.rl.setBackgroundColor(bgColor);
		
		return convertView;
	}
	
	private void mergeMsgs(){
		int n = 0;
		int m = 0;
		MsgHolder mg = null;
		MsgHolder st = null;
		while (m<rcvd.size() || n<sent.size()){
			if (m<rcvd.size()){
				mg = rcvd.get(rcvd.size() - m - 1);
				mg.sender = "them";
			}
			if (n<sent.size()){
				st = sent.get(sent.size() - n - 1);
				st.sender = "me";
			}
			
			if (mg != null && st != null){
				if ((Long.parseLong(mg.d) < Long.parseLong(st.d) && m < rcvd.size()) || n >= sent.size()){
					all.add(mg);
					m++;
				}else {
					all.add(st);
					n++;
				}
			}else if(mg == null){
				all = sent;
				break;
			}else if(st == null){
				all = rcvd;
				break;
			}
		}
	}

	static class MsgView{
		RelativeLayout rl;
		TextView msg;
		TextView date;
	}
}
