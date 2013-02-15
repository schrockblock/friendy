package org.ell.text;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class PplAdapter extends BaseAdapter implements OnClickListener{
	Vector<Person> people;
	Context context;
	
	public PplAdapter(Context ctxt, Vector<Person> ppl){
		this.context = ctxt;
		this.people = ppl;
	}

	@Override
	public int getCount() {
		return people.size();
	}

	@Override
	public Object getItem(int arg0) {
		return people.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Person p = (Person)getItem(position);
		
		PersonHolder holder;
		
		if (convertView == null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			convertView = inflater.inflate(R.layout.person, parent, false);
			
			holder = new PersonHolder();
			
			holder.imgBtn = (ImageButton)convertView.findViewById(R.id.img_btn);
			Drawable d = context.getResources().getDrawable(R.drawable.android);
			holder.imgBtn.setImageDrawable(d);
			float scale = context.getResources().getDisplayMetrics().density;
			holder.imgBtn.setMaxHeight((int)(100*scale));
			holder.imgBtn.setMaxWidth((int)(100*scale));
			
			holder.nameTV = (TextView)convertView.findViewById(R.id.person_name);
			holder.nameTV.setTypeface(Typeface.DEFAULT_BOLD);
			if (p.unread){
				//holder.nameTV.setTextColor(Color.BLUE);
			}
			
			convertView.setTag(holder);
		}else{
			holder = (PersonHolder)convertView.getTag();
		}

		if (p.dName != null){
			holder.nameTV.setText(p.dName);
		}else{
			holder.nameTV.setText(p.n);
		}
		if (p.bmp != null){
			holder.imgBtn.setImageBitmap(p.bmp);
		}else{
			Drawable d = context.getResources().getDrawable(R.drawable.android);
			holder.imgBtn.setImageDrawable(d);
		}
		holder.imgBtn.setId(position);
		holder.imgBtn.setOnClickListener(this);
		
		return convertView;
	}
	
	class PersonHolder {
		TextView nameTV;
		ImageButton imgBtn;
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(context, Msgs.class);
		i.putExtra("id", people.get(v.getId()).n);
		i.putExtra("new", false);
		context.startActivity(i);
	}

}
