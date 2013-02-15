package org.ell.text;

import java.util.Vector;

import android.app.Application;

public class FriendyApp extends Application {
	Vector<Person> people = new Vector<Person>();
	
	
	@Override
    public void onCreate() {
		super.onCreate();
	}
	
	public void setPpl(Vector<Person> p){
		people=p;
	}
	
	public Vector<Person> getPpl(){
		return people;
	}
	
	public void addText(){
		
	}
	
	public Person findNum(String num){
		Person p = new Person(num);
		return people.get(people.indexOf(p));
	}
	
	public int findIndex(String num){
		Person p = new Person(num);
		return people.indexOf(p);
	}
}
