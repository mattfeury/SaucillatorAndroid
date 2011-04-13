package com.mattfeury.saucillator.android;

import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;

/*
 * This is my main class to test my engine. There is some pretty strange stuff here as it
 * is mostly a playground for new ideas, many of which were implementented hastily as a proof of concept.
 * 
 * The basic end idea is to use your Android device as a Kaosscilator type instrument.
 * 
 */
public class TouchTest extends Activity implements OnTouchListener, SensorEventListener {

    private static final String TAG = "Sauce" ;
    private Panel p;
    public static float x,y;
    private float freq = 440;
    
    //synth elements
    private Sine sine;	
    Dac dac;
	WtOsc ugOscA1, ugOscA2;
	private LinkedList<UGen> oscs = new LinkedList<UGen>();

	private SensorManager sensorManager = null;
	
	
	//graphics elements
	private HashMap<Integer, Finger> fingers = new HashMap<Integer, Finger>();

	
    private float BASE_FREQ = 220;
    public static int TRACKPAD_GRID_SIZE = 12;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "TouchTest");
        super.onCreate(savedInstanceState);

        //        setContentView(R.layout.main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        p = new Panel(this);
        setContentView(p);
        p.setOnTouchListener(this);
        
        //start engine
        
        //old style
        //sine = new Sine(freq);
        //new Thread(sine).start();

        Thread t = new Thread() {
      	  public void run() {
      	    try {
      	    	

      	    	ugOscA1 = new WtOsc();
      	    	ugOscA2 = new WtOsc();
      	    	
      	    	oscs.add(ugOscA1);
      	    	oscs.add(ugOscA2);
      	    	
      	    	ExpEnv ugEnvA = new ExpEnv();
      	    	
      	    	ugOscA1.fillWithSin();
      	    	ugOscA2.fillWithSqrWithAmp(0.5f);
      	    	
      	    	dac = new Dac();

      	    	Delay ugDelay = new Delay(UGen.SAMPLE_RATE/2);
      	    	
      	    	ugEnvA.chuck(dac);
      	    	//ugEnvA.chuck(ugDelay);
      	    	ugDelay.chuck(ugEnvA);
      	    	
      	    	//ugOscA1.chuck(ugEnvA);
      	    	ugOscA2.chuck(ugDelay);
      	    	ugOscA1.chuck(ugDelay);
      	    	
      	    	ugEnvA.setFactor(ExpEnv.hardFactor);
      	    	ugEnvA.setActive(true);
      	    	ugOscA1.setFreq(440.0f);
      	    	ugOscA2.setFreq(880f);
      	    	dac.open();        	    	

      	      //float freq = 440f;
      	      while (true) {
      	    	//freq += 1f;
        	    //	ugOscA1.setFreq(freq);
        	    	dac.tick();
      	      }
      	    }
      	    catch(Exception ex) {
      	    	Log.i(TAG, "bad time " + ex);
      	    	dac.close();
      	    }
      	  }
      	};
      	
      t.start();

    }
    
 // This method will update the UI on new sensor events
    public void onSensorChanged(SensorEvent sensorEvent) {
     synchronized (this) {
      if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
    	  Log.i(TAG, "accel1 " + sensorEvent.values[0]);
    	  Log.i(TAG, "accel2 " + sensorEvent.values[1]);
    	  Log.i(TAG, "accel3 " + sensorEvent.values[2]);
      }
      
      if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
    	  //Log.i(TAG, "o1 " + sensorEvent.values[0]);
    	  //Log.i(TAG, "o2 " + sensorEvent.values[1]);
    	  float pan = sensorEvent.values[2] / 80;
    	  //Log.i(TAG, "o3 " + sensorEvent.values[2]);
    	  if(pan <= .1 && pan >= -.1)
    		dac.setPan(1.0f, 1.0f);
    	  else if(pan < 0)
    	    dac.setPan(Math.abs(pan), 0f);
    	  else
    		dac.setPan(0f, pan);
      }
     }
    }
   
    // I've chosen to not implement this method
    public void onAccuracyChanged(Sensor arg0, int arg1) {
	  // TODO Auto-generated method stub
	  
	 }
    
    @Override
    protected void onResume() {
     super.onResume();
     // Register this class as a listener for the accelerometer sensor
     //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
     // ...and the orientation sensor
     sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
    }
   
    @Override
    protected void onStop() {
     // Unregister the listener
     sensorManager.unregisterListener(this);
     super.onStop();
    } 
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      // Handle touch events here...
      dumpEvent(event);
      int maxHeight = v.getHeight();
      int maxWidth = v.getWidth();

      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;

      
      if (actionCode == MotionEvent.ACTION_UP && dac.isPlaying()) {      //last finger lifted. stop playback
        //dac.toggle();
    	for(UGen osc : oscs)
    		((WtOsc)osc).stop();
    	
        fingers.clear();
        p.invalidate();
        return true;
      }
      //if (actionCode == MotionEvent.ACTION_DOWN && ! dac.isPlaying()) { //first finger pressed. start DAC.
        //dac.toggle();
      //}
      
      //each finger
      for (int i = 0; i < event.getPointerCount(); i++) {
    	  int id = event.getPointerId(i);
    	  Finger f = new Finger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
    	  fingers.put((Integer)id, f);  
        
          
          //final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;


	      //make noise
    	  WtOsc sine = (WtOsc)oscs.get(id);
	      if(! sine.isPlaying())
	        sine.togglePlayback(); //play if we were stopped
	      
	      float thisY = event.getY(i);
	      
	      if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
	        updateFrequency(id, (int)((maxHeight - thisY) / maxHeight * TRACKPAD_GRID_SIZE));
	      } else { //kill
	    	fingers.remove((Integer)i);
	    	sine.togglePlayback();
	      }
      }
      p.invalidate();

      return true; // indicate event was handled
    }

    public void updateFrequency(int sineKey, int offset) //0-trackpadsize
    {
    	((WtOsc)oscs.get(sineKey)).setFreq(BASE_FREQ * (sineKey+1.0f) * (float) Math.pow(2, offset / 12.0));
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
       String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
          "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
       StringBuilder sb = new StringBuilder();
       int action = event.getAction();
       int actionCode = action & MotionEvent.ACTION_MASK;
       sb.append("event ACTION_" ).append(names[actionCode]);
       if (actionCode == MotionEvent.ACTION_POINTER_DOWN
             || actionCode == MotionEvent.ACTION_POINTER_UP) {
          sb.append("(pid " ).append(
          action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
          sb.append(")" );
       }
       sb.append("[" );
       for (int i = 0; i < event.getPointerCount(); i++) {
          sb.append("#" ).append(i);
          sb.append("(pid " ).append(event.getPointerId(i));
          sb.append(")=" ).append((int) event.getX(i));
          sb.append("," ).append((int) event.getY(i));
          if (i + 1 < event.getPointerCount())
             sb.append(";" );
       }
       sb.append("]" );
       Log.i(TAG, sb.toString());
    }
    
    class Panel extends View {
        public Panel(Context context) {
            super(context);
        }
 
        @Override
        public void onDraw(Canvas canvas) {
            Log.i("touch", "started");
            canvas.drawColor(Color.WHITE);
            for(Finger f : fingers.values())
            	f.draw(canvas);

        }
    }

    
}
