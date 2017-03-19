package com.zoomimageview;


import android.app.Activity;
import android.os.Bundle;



public class MainActivity extends Activity {
	private ZoomImageView zoomImageview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		zoomImageview=(ZoomImageView) findViewById(R.id.zoomimage);
		zoomImageview.setBitmap(R.mipmap.sa);
	}

}
