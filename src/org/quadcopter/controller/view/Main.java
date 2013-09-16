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
	
	private static Quadcopter quad;
	private VerticalSeekBar axis_z, axis_y;
	private SeekBar axis_x, axis_rotate;
	
	private Button turn_on_off;
	private boolean motors_on = false;
	private static final String TAG_MOTORS_STATE = "motors_state";
	
	public static final String KEY_SETTINGS = "settings";
	private static final int SETTINGS_REQUEST = 12;
	private SettingsData settingsData = new SettingsData();

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
		
		turn_on_off = (Button) findViewById(R.id.btn_turn_on_off);
		turn_on_off.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				motors_on = !motors_on;
				axis_z.setEnabled(motors_on);
				axis_z.setProgress(0);

				if (motors_on == true)
					quad.requestMove('z', axis_z.getProgress()+1000);
				else
					quad.requestMove('z', 0);
				updateLabelBtn();
			}
		});

		if (savedInstanceState != null)
			motors_on = savedInstanceState.getBoolean(TAG_MOTORS_STATE);
		axis_z.setEnabled(motors_on);
		updateLabelBtn();


		if (quad == null) {
			quad = new Quadcopter(this);
			showProgress();
		}
		else
			quad.controllerSet(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		quad.requestOrientation(false);
	}
	
	private void updateLabelBtn()
	{
		if (motors_on == true)
			turn_on_off.setText(this.getString(R.string.turn_off));
		else
			turn_on_off.setText(this.getString(R.string.turn_on));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(TAG_MOTORS_STATE, motors_on);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		char axis = 0;
		
		if (seekBar == axis_z)
			return;
		
		if (seekBar == axis_y) {
			axis = 'y';
			progress = progress - 3;
		}
		else if (seekBar == axis_x) {
			axis = 'x';
			progress = progress - 3;
		}
		else if (seekBar == axis_rotate) {
			axis = 'r';
			progress = progress - 1;
		}
		
		if (axis == 0)
			return;
		
		Log.d(Main.TAG, "axis="+axis+" progress="+progress);
		quad.requestMove(axis, progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar == axis_z)
		{
			quad.requestMove('z', seekBar.getProgress()+1000);
			Log.d(Main.TAG, "final z progress="+(seekBar.getProgress()+1));
		}

		if (seekBar == axis_y || seekBar == axis_x) {
			seekBar.setProgress(3);
		} else if (seekBar == axis_rotate) {
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
	public void connectedQuad() {
		Log.d(Main.TAG, "connected");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		});
		quad.requestConfigs();
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
			quad.requestOrientation(true);
			startActivity(intent);
			return true;
		case R.id.settings_menu_option:
			intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(KEY_SETTINGS, settingsData);
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
		quad.writeConfigs(settings);
	}

	@Override
	public void reponseConfig(SettingsData settings) {
		settingsData = settings;
	}

	@Override
	public void reponseWriteConfig() {
		quad.requestConfigs();
	}
}
