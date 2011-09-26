package com.mattfeury.saucillator.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.Random;

import com.sauce.touch.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

/*
 * Main activity for the App. This will spin up the visuals and the audio engine. The audio engine gets
 * its own thread.
 */
public class TouchTest extends Activity implements OnTouchListener, SensorEventListener {
    private static final String TAG = "Sauce";
    private Panel p;
    FractalGen fractGen;
    float fX = 0, fY = 0; //fractal x and y coords
    Paint backColor;

    //defaults
    private int delayRate = UGen.SAMPLE_RATE / 4;
    private int lag = (int)(DEFAULT_LAG * 100);
    private String fileName = "Recording";
    private int note = 0;
    private int octave = 4;
    private boolean visuals = false;

    //music shtuffs
    public int[] scale = Instrument.pentatonic;

    public final static int MOD_RATE_MAX = 20;
    public final static int MOD_DEPTH_MAX = 1000;
    public final static float DEFAULT_LAG = 0.5f;
    public static int TRACKPAD_GRID_SIZE = 12;

    private boolean init = false;
    
    //synth elements
    private Dac dac;
    private Oscillator osc;
    private Oscillator osc2;
    private ExpEnv ugEnvA;
    private Delay ugDelay;

    private SensorManager sensorManager = null;
    MediaPlayer secretSauce;

    //graphics elements
    private HashMap<Integer, Finger> fingers = new HashMap<Integer, Finger>();
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Brewing sauce...");

        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        secretSauce = MediaPlayer.create(this, R.raw.sauceboss);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        p = new Panel(this);
        setContentView(p);
        p.setOnTouchListener(this);
        backColor = new Paint();
        
        Thread t = new Thread() {
      	  public void run() {
      	    try {
              osc = new SingingSaw();
              osc2 = new Sine();

      	    	ugEnvA = new ExpEnv();

      	    	dac = new Dac();

      	    	ugDelay = new Delay(delayRate);

      	    	ugDelay.chuck(dac);
      	    	ugEnvA.chuck(ugDelay);

      	    	osc2.chuck(ugEnvA);
      	    	osc.chuck(ugEnvA);

      	    	ugEnvA.setFactor(ExpEnv.hardFactor);
      	    	ugEnvA.setActive(true);
      	    	dac.open();
              init = true;

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
    
    /**
     * Update parameters based on the accelerometer.
     * This is currently not hooked up because it breaks Gingerbread and
     * using the accelerometer may not be a great method for controlling things (it is volatile).
     */
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
    
    /**
     * On resume of the app.
     * Most of the accelerometer stuff has been disabled for the time being.
     */
    @Override
    protected void onResume() {
     super.onResume();
     // Register this class as a listener for the accelerometer sensor
     //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
     
     // ...and the orientation sensor
     // FIXME this breaks in Gingerbread it seems
     //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
    }
   
    /**
     * Update parameters based on the settings menu.
     */
    private void updateSettings() {
      float newFreq = Instrument.getFrequencyForNote(note + 1, octave);
      osc.setBaseFreq(newFreq);
      osc2.setBaseFreq(newFreq);

      if (delayRate == 0) {
        ugDelay.updateRate(1); //i'm a hack. FIXME rate of 0 doesn't work
      } else {
        ugDelay.updateRate(delayRate);
      }
      
      osc.setLag(lag / 100f);
      osc2.setLag(lag / 100f);
    }

    @Override
    protected void onStop() {
     // Unregister the listener
     
     //FIXME this breaks the app in Gingerbread (see above)
     //sensorManager.unregisterListener(this);
     //android.os.Process.killProcess(android.os.Process.myPid());

     //TODO stop the DAC here or something
     // remember: this is also called when we goto settings
     super.onStop();
    } 
    
    
    protected void onDestroy() {
    	android.os.Process.killProcess(android.os.Process.myPid());
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
    
    /**
     * That main goodness. Handles touch events and gets properties of them to change the oscillators
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (! init) return false;

      int maxHeight = v.getHeight();
      int maxWidth = v.getWidth();

      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      
      if (actionCode == MotionEvent.ACTION_UP && dac.isPlaying()) { //last finger lifted. stop playback
        osc.stop();
        osc2.stop();

        fingers.clear();
        p.invalidate();
        return true;
      }

      if (event.getPointerCount() == 5) secretSauce.start(); //the secret sauce

      //loop through each finger      
      for (int i = 0; i < event.getPointerCount(); i++) {
        int id = event.getPointerId(i);
        float y = event.getY(i);
        float x = event.getX(i);

        updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
        
        //make noise
        if (id == 0 || id == 1) { //update sine
        	Oscillator osc = (id == 0) ? this.osc : this.osc2; //which osc does this finger correspond to?
          if(! osc.isPlaying())
            osc.togglePlayback(); //play if we were stopped

          if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
            updateFrequency(id, (int)((maxHeight - y) / maxHeight * TRACKPAD_GRID_SIZE));
            updateAmplitude(id, x / maxWidth);
          } else { //kill
            fingers.remove((Integer)i);
            osc.togglePlayback();
          }
        } else if (id == 2) { //lfo
          //TODO make this iterate or something
          osc.setModRate((int)(x / maxWidth * MOD_RATE_MAX));
          osc.setModDepth((int)((maxHeight - y) / maxHeight * MOD_DEPTH_MAX));
          osc2.setModRate((int)(x / maxWidth * MOD_RATE_MAX));
          osc2.setModDepth((int)((maxHeight - y) / maxHeight * MOD_DEPTH_MAX));
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

    /**
     * Methods for our settings menu and stuff
     */
    public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
    	switch (item.getGroupId()) {
    		case R.id.instrumentsA:
    			return instrumentSelection(item, 0);
    		case R.id.instrumentsB:
    			return instrumentSelection(item, 1);
    		case R.id.scales:
    			return scaleSelection(item);
    		default:
    	}
    	switch (item.getItemId()) {
    		case R.id.quit:
    			onDestroy();
    			return true;
    		case R.id.settings:
    			return launchSettings();
    		case R.id.record:
    			return record(item);
    		default:
    	}
        return false;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && data != null) {
          Bundle extras = data.getExtras();
          if (extras != null) {
            WavWriter.filePrefix = extras.getString("file name");
            note = extras.getInt("note");
            octave = extras.getInt("octave");
            lag = extras.getInt("lag");
            delayRate = extras.getInt("delay rate");
            visuals = extras.getBoolean("visuals");
            updateSettings();
          }
        }
    }
   
    private boolean launchSettings() {
    	Intent intent = new Intent(TouchTest.this, Settings.class);
    	intent.putExtra("octave", octave);
    	intent.putExtra("note", note);
    	intent.putExtra("file name", fileName);
    	intent.putExtra("delay rate", delayRate);
    	intent.putExtra("lag", lag);
    	intent.putExtra("visuals", visuals);
    	startActivityForResult(intent, 0);
    	return true;
    }
    
    private boolean record(MenuItem item) {
      boolean isRecording = dac.toggleRecording();
    	if (isRecording) {
    	    item.setTitle("Stop Recording");
    	    item.setIcon(R.drawable.ic_grey_rec);
            Toast.makeText(this, "Recording.", Toast.LENGTH_SHORT).show();
    	}
    	else {
		    item.setTitle("Record");
		    item.setIcon(R.drawable.ic_rec);
	        Toast.makeText(this, "Stopped Recording.", Toast.LENGTH_SHORT).show();
		    	
	        if(WavWriter.getLastFile() == null)
	          return false;
		    	
	        Intent intent = new Intent(Intent.ACTION_SEND).setType("audio/*");
		    	intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(WavWriter.getLastFile()));
		    	startActivity(Intent.createChooser(intent, "Share to"));
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

      Oscillator oldOsc = oscNum == 0 ? this.osc : this.osc2;
      oldOsc.unchuck(ugEnvA);
      //TODO kill old maybe? make sure it gets garbage collected

      switch (instrumentId) {
        case R.id.singingsaw: //singing saw
          oldOsc = new SingingSaw();
          break;
        case R.id.sine: //sine
          oldOsc = new Sine();
          break;
        case R.id.square: //square
        	oldOsc = new Square(1.0f);
          break;
        case R.id.saw: //saw
          oldOsc = new Saw(1.0f);
          break;
        default:
      }

      if (oscNum == 0) {
        this.osc = oldOsc;
        this.osc.chuck(ugEnvA);
      } else {
        this.osc2 = oldOsc;
        this.osc2.chuck(ugEnvA);
      }
    	
    	return false;
    }
    
    public void updateAmplitude(int key, float amp) {
      Log.i(TAG, "" + amp);
      Oscillator osc = (key == 0) ? this.osc : this.osc2;
      if (osc != null)
        osc.setAmplitude(amp);
    }      
    public void updateFrequency(int sineKey, int offset) {
      Oscillator osc = (sineKey == 0) ? this.osc : this.osc2;
      if (osc != null)
        osc.setFreqByOffset(scale, offset);
    }

    class Panel extends View {
        public Panel(Context context) {
            super(context);
        }
 
        @Override
        public void onDraw(Canvas canvas) {
        	if (fractGen == null)
        		fractGen = new FractalGen(canvas);
 
            fX = (fingers.values().size() > 0 ? 0 : fX);
            fY = (fingers.values().size() > 0 ? 0 : fY);

            for(Finger f : fingers.values()){
            	if(f.id == 0){
            		fX += f.x;
            		fY += f.y;
            	}
            	else{
            		backColor.setColor(Color.HSVToColor(new float[]{(f.x / canvas.getWidth())* 360, f.y / canvas.getHeight(), f.y / canvas.getHeight()}));
            		fractGen.paint.setColor(Color.HSVToColor(new float[]{360 - (f.x / canvas.getWidth()* 360), 1f - f.y / canvas.getHeight(), 1f - f.y / canvas.getHeight()}));
            	}
            }
            
            fX /= (fingers.values().size() > 0 ? fingers.values().size() : 1);
            fY /= (fingers.values().size() > 0 ? fingers.values().size() : 1);
            
            
            canvas.drawColor(backColor.getColor());
            
            if(visuals)		
            	fractGen.drawFractal(new ComplexNum(fractGen.toInput(fX, true), fractGen.toInput(fY, false)), new ComplexNum(0,0), -1);
            
            for(Finger f : fingers.values())
            	f.draw(canvas);
        }
    }

    
}
