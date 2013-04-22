package org.quadcopter.controller.view;
import org.quadcopter.controller.R;
import org.quadcopter.controller.view.util.VerticalSeekBar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Main extends Activity implements OnSeekBarChangeListener {
	private static final String TAG = "MainActiviy";
	
	private VerticalSeekBar axis_z, axis_y;
	private SeekBar axis_x, axis_rotate;
	
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

}
