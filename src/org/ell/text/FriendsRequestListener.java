package org.ell.text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class FriendsRequestListener implements AsyncFacebookRunner.RequestListener{

	@Override
	public void onComplete(String response, Object state) {
		// TODO Auto-generated method stub
		String blank = response;
	}

	@Override
	public void onFacebookError(FacebookError e, Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIOException(IOException e, Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMalformedURLException(MalformedURLException e, Object state) {
		// TODO Auto-generated method stub
		
	}

	

}
