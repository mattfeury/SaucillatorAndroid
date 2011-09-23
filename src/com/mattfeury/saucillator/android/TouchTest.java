package com.mattfeury.saucillator.android;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

import com.sauce.touch.R;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private static final String TAG = "Sauce";
    private Panel p;

    //music shtuffs
    public int[] scale = Instrument.pentatonic;

    //synth elements
    Dac dac;
    WtOsc ugOscA1, ugOscA2;
    private LinkedList<WtOsc> oscs = new LinkedList<WtOsc>();

    private SensorManager sensorManager = null;

    //graphics elements
    private HashMap<Integer, Finger> fingers = new HashMap<Integer, Finger>();
    private Map<Integer,String> instruments = new HashMap<Integer,String>() {{
        put(0, "sine");
        put(1, "square");
        put(2, "saw");
    }};
	
    private int BASE_FREQ = 440;
    public static int TRACKPAD_GRID_SIZE = 12;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
      Log.i(TAG, "TouchTest");
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        p = new Panel(this);
        setContentView(p);
        p.setOnTouchListener(this);

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
      	    	dac.open();

      	      while (true) {
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
     // FIXME this breaks in Gingerbread it seems
     //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
    }
   
    @Override
    protected void onStop() {
     // Unregister the listener
     
     //FIXME this breaks the app in Gingerbread (see above)
     //sensorManager.unregisterListener(this);
     android.os.Process.killProcess(android.os.Process.myPid());
     super.onStop();
    } 
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void updateOrCreateFinger(int id, float x, float y, float size, float pressure) {
      Finger maybe = fingers.get((Integer)id);
      if (maybe != null) {
        maybe.update(x, y, size, pressure);
      } else {
        Finger f = new Finger(id, x, y, size, pressure);
        fingers.put((Integer)id, f);
      }
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      // Handle touch events here...
      //dumpEvent(event);
      int maxHeight = v.getHeight();
      int maxWidth = v.getWidth();

      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      
      if (actionCode == MotionEvent.ACTION_UP && dac.isPlaying()) {      //last finger lifted. stop playback
        //dac.toggle();
        for(WtOsc osc : oscs)
          osc.stop();
    	
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

        if ((id + 1) > oscs.size()) break;

        updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));

        //make noise
        WtOsc sine = oscs.get(id);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
    	switch (item.getGroupId()) {
    		case R.id.instrumentsA:
    			return instrumentSelection(item, 0);
    		case R.id.instrumentsB:
    			return instrumentSelection(item, 1);
    		case R.id.scales:
    			return scaleSelection(item);
    		case R.id.toggles:
    			return toggleSelection(item);
    		default:
    	}
    	if (item.getItemId() == R.id.quit)
    		onStop();
        return false;
    }
    
    private boolean toggleSelection(MenuItem item) {
    	item.setChecked(!item.isChecked());
    	switch (item.getItemId()) {
    		case R.id.toggle_delay:
          dac.toggleRecording();
    		default:
    	}
    	return true;
    }
    
    private boolean scaleSelection(MenuItem item) {
    	if (item.isChecked()) {
    		return true;
    	}
    	item.setChecked(true);
      int scaleId = item.getItemId();

      switch (scaleId) {
        case R.id.pentatonic: //pentatonic
          scale = Instrument.pentatonic;
          break;
        case R.id.major: //major
          scale = Instrument.majorScale;
          break;
        case R.id.minor: //minor
          scale = Instrument.minorScale;
          break;
        case R.id.blues: //blues
          scale = Instrument.minorBluesScale;
          break;
        case R.id.chromatic: //chromatic
          scale = Instrument.chromaticScale;
          break;
        default:
      }
    	
    	return false;
    }
    
    private boolean instrumentSelection(MenuItem item, int oscNum) {
    	if (item.isChecked()) {
    		return true;
    	}
    	item.setChecked(true);
      int instrumentId = item.getItemId();

      WtOsc osc;
      try {
        osc = oscs.get(oscNum);
      } catch(Exception e){
        return false;
      }

      switch (instrumentId) {
        case R.id.sine: //sine
          osc.fillWithSin();
          break;
        case R.id.square: //square
          osc.fillWithSqr();
          break;
        case R.id.saw: //saw
          osc.fillWithSaw();
          break;
        default:
      }
    	
    	return false;
    }
    
    public void updateFrequency(int sineKey, int offset) //0-trackpadsize
    {
    	WtOsc osc = oscs.get(sineKey);

      //TODO should we set the two oscillators an octave apart? probs
      float freq = Instrument.getFrequencyForScaleNote(scale, (int)((sineKey+1.0) * BASE_FREQ), offset);
      osc.setFreq(freq);
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
            canvas.drawColor(Color.BLACK);
            for(Finger f : fingers.values())
            	f.draw(canvas);
        }
    }

    
}
