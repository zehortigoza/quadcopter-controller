package org.quadcopter.controller.view;

import org.quadcopter.controller.R;
import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.controller.Sensors;
import org.quadcopter.controller.view.util.CubeRenderer;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
 
public class CubeActivity extends Activity implements Sensors {
	private CubeRenderer renderer;
	private TextView mText;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	FrameLayout frame;
    	GLSurfaceView view;
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.cube);
        
        renderer = new CubeRenderer();
        view = new GLSurfaceView(this);
        view.setRenderer(renderer);
        
        frame = (FrameLayout)findViewById(R.id.cube);
        frame.addView(view);
        
        mText = (TextView)findViewById(R.id.angles_text);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Quadcopter.setSensorActivity(this);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	Quadcopter.setSensorActivity(null);
    }

	@Override
	public void updateOrientation(final float roll, final float pitch, final float yaw) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mText.setText("Roll="+roll+" Pitch="+pitch+" Yaw="+yaw);
			}
		});
		renderer.updateOrientation(roll, pitch, yaw);
	}
}