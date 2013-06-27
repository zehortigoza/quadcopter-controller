package org.quadcopter.controller.view;
import org.quadcopter.controller.R;
import org.quadcopter.controller.controller.ControllerActivity;
import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.util.VerticalSeekBar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Main extends Activity implements OnSeekBarChangeListener, ControllerActivity {
	private static final String TAG = "MainActiviy";
	
	private VerticalSeekBar axis_z, axis_y;
	private SeekBar axis_x, axis_rotate;
	private static Quadcopter quad;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		axis_z = (VerticalSeekBar) findViewById(R.id.seek_bar_axis_z);
		axis_z.setOnVerticalSeekBarChangeListener(this);
		
		axis_y = (VerticalSeekBar) findViewById(R.id.seek_bar_axis_y);
		axis_y.setOnVerticalSeekBarChangeListener(this);
		
		axis_x = (SeekBar) findViewById(R.id.seek_bar_axis_x);
		axis_x.setOnSeekBarChangeListener(this);
		
		axis_rotate = (SeekBar) findViewById(R.id.seek_bar_rotate);
		axis_rotate.setOnSeekBarChangeListener(this);
		
		if (quad == null)
			quad = new Quadcopter(this);
		else
			quad.ControllerActivitySet(this);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		Log.d(TAG, "progress="+progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar == axis_y || seekBar == axis_x) {
			seekBar.setProgress(3);
		} else if (seekBar == axis_rotate) {
			seekBar.setProgress(1);
		}
	}

	@Override
	public void pingRequest(int num) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void pingResponse(int num) {
		quad.pingRequest(num+1);		
	}

	@Override
	public void batteryResponse(int percent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void radioLevelResponse(int value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveResponse() {
		// TODO Auto-generated method stub
	}

	@Override
	public void gyroResponse(int x, int y, int z) {
		Log.d("quad", "Gyro "+x+" "+y+" "+z);
	}

	@Override
	public void accelReponse(int x, int y, int z) {
		Log.d("quad", "Accel "+x+" "+y+" "+z);
	}

	@Override
	public void calibrateReponse() {
		// TODO Auto-generated method stub
	}
}
