package org.ell.text;

public class MsgHolder {
	public String sender;
	public String d;
	public String b;
	public boolean unread;
	
	public MsgHolder(String date, String body){
		d=date;
		b=body;
	}

	@Override
	public boolean equals(Object c){
		if (c==this){
			return true;
		}
		if (!(c instanceof MsgHolder)){
			return false;
		}
		if (((MsgHolder)c).b.equals(this.b)&&((MsgHolder)c).d.equals(this.d)){
			return true;
		}
		return false;
	}
}
