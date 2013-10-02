package org.quadcopter.controller.view;

import org.quadcopter.controller.R;
import org.quadcopter.controller.controller.Quadcopter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class EscActivity extends Activity {
	private Quadcopter mQuad = Quadcopter.getInstance();
	
	private SeekBar mValue;
	private ToggleButton mFrontLeft;
	private ToggleButton mFrontRight;
	private ToggleButton mBackLeft;
	private ToggleButton mBackRight;
	private Button mSend;
	
	private void sendValues() {
		int fl, fr, bl, br, value;
		
		fl = fr = bl = br = 0;
		value = (mValue.getProgress()+1)*1000;
		Log.d("quad", "******* mValue.getProgress()="+mValue.getProgress()+" value="+value);
		
		if (mFrontLeft.isChecked())
			fl = value;
		
		if (mFrontRight.isChecked())
			fr = value;
		
		if (mBackLeft.isChecked())
			bl = value;
		
		if (mBackRight.isChecked())
			br = value;
		
		mQuad.setEscValues(fl, fr, bl, br);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mQuad.setEscConfigurationMode(true);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.esc);
		
		mValue = (SeekBar)findViewById(R.id.seek_bar_value);
		mFrontLeft = (ToggleButton)findViewById(R.id.front_left);
		mFrontRight = (ToggleButton)findViewById(R.id.front_right);
		mBackLeft = (ToggleButton)findViewById(R.id.back_left);
		mBackRight = (ToggleButton)findViewById(R.id.back_right);
		mSend = (Button)findViewById(R.id.send);
		
		mSend.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendValues();				
			}
		});
	}

}
