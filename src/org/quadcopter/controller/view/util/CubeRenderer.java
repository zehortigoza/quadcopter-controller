package org.quadcopter.controller.view.util;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.quadcopter.controller.controller.Sensors;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
 
public class CubeRenderer implements Renderer, Sensors {
 
    private Cube mCube = new Cube();
    
    private float basePitch = 0;
    private float baseRoll = 0;
    private float baseYaw = 0;
    
    public void updateOrientation(float roll, float pitch, float yaw) {
    	baseRoll = roll;
    	basePitch = pitch;
    	baseYaw = yaw;
    }
 
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
             
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
 
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                  GL10.GL_NICEST);
             
    }
 
    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);       
        gl.glLoadIdentity();
         
        gl.glTranslatef(0.0f, 0.0f, -10.0f);//Move z units into the screen
        
        //Rotate around the axis based on the rotation matrix (rotation, x, y, z)
        gl.glRotatef(baseRoll, 0.0f, 1.0f, 0.0f);
      	gl.glRotatef(basePitch, 1.0f, 0.0f, 0.0f);
      	gl.glRotatef(baseYaw, 0.0f, 0.0f, 1.0f);
             
        mCube.draw(gl);
            
        gl.glLoadIdentity();
    }
 
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
        gl.glViewport(0, 0, width, height);
 
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}