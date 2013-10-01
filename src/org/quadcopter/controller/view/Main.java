package org.quadcopter.controller.view;
import java.io.Serializable;

import org.quadcopter.controller.R;
import org.quadcopter.controller.controller.Controller;
import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.util.VerticalSeekBar;
import org.quadcopter.model.SettingsData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Main extends Activity implements OnSeekBarChangeListener, Controller {
	public static final String TAG = "quad";
	
	private static Quadcopter sQuad = Quadcopter.getInstance();
	private VerticalSeekBar mAxisZ, mAxisY;
	private SeekBar mAxisX, mAxisRotate;
	
	private Button mBtnOnOff;
	private boolean mMotorsOn = false;
	private static final String TAG_MOTORS_STATE = "motors_state";
	
	public static final String KEY_SETTINGS = "settings";
	private static final int SETTINGS_REQUEST = 12;
	private SettingsData mSettingsData = new SettingsData();

	private ProgressDialog mProgressDialog = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mAxisZ = (VerticalSeekBar) findViewById(R.id.seek_bar_axis_z);
		mAxisZ.setOnVerticalSeekBarChangeListener(this);
		
		mAxisY = (VerticalSeekBar) findViewById(R.id.seek_bar_axis_y);
		mAxisY.setOnVerticalSeekBarChangeListener(this);
		
		mAxisX = (SeekBar) findViewById(R.id.seek_bar_axis_x);
		mAxisX.setOnSeekBarChangeListener(this);
		
		mAxisRotate = (SeekBar) findViewById(R.id.seek_bar_rotate);
		mAxisRotate.setOnSeekBarChangeListener(this);
		
		mBtnOnOff = (Button) findViewById(R.id.btn_turn_on_off);
		mBtnOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMotorsOn = !mMotorsOn;
				mAxisZ.setEnabled(mMotorsOn);
				mAxisZ.setProgress(0);

				if (mMotorsOn == true)
					sQuad.requestMove('z', mAxisZ.getProgress()+1000);
				else
					sQuad.requestMove('z', 0);
				updateLabelBtn();
			}
		});

		if (savedInstanceState != null)
			mMotorsOn = savedInstanceState.getBoolean(TAG_MOTORS_STATE);
		mAxisZ.setEnabled(mMotorsOn);
		updateLabelBtn();

		sQuad.setController(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		sQuad.requestOrientation(false);
	}
	
	private void updateLabelBtn()
	{
		if (mMotorsOn == true)
			mBtnOnOff.setText(this.getString(R.string.turn_off));
		else
			mBtnOnOff.setText(this.getString(R.string.turn_on));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(TAG_MOTORS_STATE, mMotorsOn);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		char axis = 0;
		
		if (seekBar == mAxisZ)
			return;
		
		if (seekBar == mAxisY) {
			axis = 'y';
			progress = progress - 3;
		}
		else if (seekBar == mAxisX) {
			axis = 'x';
			progress = progress - 3;
		}
		else if (seekBar == mAxisRotate) {
			axis = 'r';
			progress = progress - 1;
		}
		
		if (axis == 0)
			return;
		
		Log.d(Main.TAG, "axis="+axis+" progress="+progress);
		sQuad.requestMove(axis, progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar == mAxisZ)
		{
			sQuad.requestMove('z', seekBar.getProgress()+1000);
			Log.d(Main.TAG, "final z progress="+(seekBar.getProgress()+1));
		}

		if (seekBar == mAxisY || seekBar == mAxisX) {
			seekBar.setProgress(3);
		} else if (seekBar == mAxisRotate) {
			seekBar.setProgress(1);
		}
	}

	@Override
	public void requestPing(int num) {
		Log.d("quad2", "request");
		// TODO Auto-generated method stub		
	}

	@Override
	public void responsePing(int num) {
		Log.d("quad2", "response");
		sQuad.requestPing(num+1);		
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
		Log.d(Main.TAG, "Gyro "+x+" "+y+" "+z);
	}

	@Override
	public void responseAccel(float x, float y, float z) {
		Log.d(Main.TAG, "Accel "+x+" "+y+" "+z);
	}

	@Override
	public void reponseCalibrate() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onQuadConnected() {
		Log.d(Main.TAG, "connected");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
		});
		sQuad.requestConfigs();
	}
	
	private void showProgress() {
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setTitle(R.string.quad_disconnected);
		mProgressDialog.show();
	}

	@Override
	public void onQuadDisconnected() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showProgress();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
		case R.id.cube_menu_option:
			intent = new Intent(this, CubeActivity.class);
			sQuad.requestOrientation(true);
			startActivity(intent);
			return true;
		case R.id.settings_menu_option:
			intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(KEY_SETTINGS, mSettingsData);
			startActivityForResult(intent, SETTINGS_REQUEST);
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Serializable serializable;
		SettingsData settings;
		
		if (requestCode != SETTINGS_REQUEST)
			return;
		if (resultCode != RESULT_OK)
			return;
		
		serializable = data.getSerializableExtra(KEY_SETTINGS);
		if (serializable == null)
			return;
		settings = (SettingsData)serializable;
		sQuad.writeConfigs(settings);
	}

	@Override
	public void onReponseConfig(SettingsData settings) {
		mSettingsData = settings;
	}

	@Override
	public void reponseWriteConfig() {
		sQuad.requestConfigs();
	}
}
