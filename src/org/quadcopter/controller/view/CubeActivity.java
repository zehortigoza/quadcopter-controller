package org.quadcopter.controller.view;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.util.CubeRenderer;
 
public class CubeActivity extends Activity {
	private CubeRenderer renderer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	GLSurfaceView view;
        super.onCreate(savedInstanceState);
        
        renderer = new CubeRenderer();
        view = new GLSurfaceView(this);
        view.setRenderer(renderer);
        setContentView(view);        
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Quadcopter.setSensorActivity(renderer);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	Quadcopter.setSensorActivity(null);
    }
}