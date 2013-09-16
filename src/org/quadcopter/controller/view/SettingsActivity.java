package org.quadcopter.controller.view;

import org.quadcopter.controller.R;
import org.quadcopter.model.SettingsData;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {
	
	private EditText pidPEdit;
	private EditText pidIEdit;
	private Button saveBtn;
	
	private SettingsData settingsData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		pidPEdit = (EditText) findViewById(R.id.pid_p_value_edit);
		pidIEdit = (EditText) findViewById(R.id.pid_i_value_edit);
		saveBtn = (Button) findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				settingsData.setPidPValue(Float.parseFloat(pidPEdit.getText().toString()));
				settingsData.setPidIValue(Float.parseFloat(pidIEdit.getText().toString()));
				returnIntent.putExtra(Main.KEY_SETTINGS, settingsData);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		settingsData = (SettingsData)getIntent().getSerializableExtra(Main.KEY_SETTINGS);
		pidPEdit.setText(""+settingsData.getPidPValue());
		pidIEdit.setText(""+settingsData.getPidIValue());
	}
}
