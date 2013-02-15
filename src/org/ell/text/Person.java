package org.ell.text;

import java.util.Vector;

import android.graphics.Bitmap;

public class Person {
	public String n;
	private Vector<MsgHolder> msgs;
	private Vector<MsgHolder> sentmsgs;
	public String picId;
	public String dName;
	public Bitmap bmp;
	public boolean unread;
	public String fbid;
	public int fbpos=-1;
	
	public Person(String number){
		n=number;
		msgs=new Vector<MsgHolder>();
		sentmsgs= new Vector<MsgHolder>();
	}
	
	public void addSentMsg(MsgHolder m){
		sentmsgs.add(m);
	}
	
	public void insertSentMsg(MsgHolder m){
		sentmsgs.add(0, m);
	}
	
	public void setSentMsgs(Vector<MsgHolder> m){
		sentmsgs=m;
	}
	
	public Vector<MsgHolder> getSentMsgs(){
		return sentmsgs;
	}
	
	public void addMsg(MsgHolder m){
		msgs.add(m);
	}
	
	public void insertMsg(MsgHolder m){
		msgs.add(0, m);
	}
	
	public void setMsgs(Vector<MsgHolder> m){
		msgs=m;
	}
	
	public Vector<MsgHolder> getMsgs(){
		return msgs;
	}

	@Override
	public boolean equals(Object c){
		if (c==this){
			return true;
		}
		if (!(c instanceof Person)){
			return false;
		}
		if (((Person)c).n.equals(this.n)){
			return true;
		}
		return false;
	}
}
