package org.quadcopter.controller.view;
import org.quadcopter.controller.R;
import org.quadcopter.controller.controller.ControllerActivity;
import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.util.VerticalSeekBar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Main extends Activity implements OnSeekBarChangeListener, ControllerActivity {
	private static final String TAG = "MainActiviy";
	
	private static Quadcopter quad;
	private VerticalSeekBar axis_z, axis_y;
	private SeekBar axis_x, axis_rotate;
	
	private ProgressDialog progressDialog = null;
	
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
		
		if (quad == null) {
			quad = new Quadcopter(this);
			showProgress();
		}
		else
			quad.controllerSet(this);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		//Log.d("quad", "progress="+progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar == axis_z)
		{
			quad.requestMove('z', seekBar.getProgress());
			Log.d("quad", "final z progress="+seekBar.getProgress());
		}
		
		if (seekBar == axis_y || seekBar == axis_x) {
			seekBar.setProgress(3);
		} else if (seekBar == axis_rotate) {
			seekBar.setProgress(1);
		}
	}

	@Override
	public void requestPing(int num) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void responsePing(int num) {
		quad.requestPing(num+1);		
	}

	@Override
	public void responseBattery(int percent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void responseRadioLevel(int value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void responseMove() {
		// TODO Auto-generated method stub
	}

	@Override
	public void responseGyro(float x, float y, float z) {
		Log.d("quad", "Gyro "+x+" "+y+" "+z);
	}

	@Override
	public void responseAccel(float x, float y, float z) {
		Log.d("quad", "Accel "+x+" "+y+" "+z);
	}

	@Override
	public void reponseCalibrate() {
		// TODO Auto-generated method stub
	}

	@Override
	public void connectedQuad() {
		Log.d("quad", "connected");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		});
	}
	
	private void showProgress() {
		if (progressDialog != null)
			progressDialog.dismiss();
		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.setTitle(R.string.quad_disconnected);
		progressDialog.show();
	}

	@Override
	public void disconnectedQuad() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showProgress();
			}
		});
	}
}
