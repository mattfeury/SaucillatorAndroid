package com.mattfeury.saucillator.android;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
public class SauceEngine extends Activity implements OnTouchListener, SensorEventListener {
    private static final String TAG = "Sauce";
    private SauceView view;

    //defaults
    private int delayRate = UGen.SAMPLE_RATE / 4;
    private int lag = (int)(DEFAULT_LAG * 100);
    private String fileName = "Recording";
    private int note = 0;
    private int octave = 4;

    //music shtuffs
    public int[] scale = Instrument.pentatonic;

    public final static int MOD_RATE_MAX = 20;
    public final static int MOD_DEPTH_MAX = 1000;
    public final static float DEFAULT_LAG = 0.5f;
    public static int TRACKPAD_GRID_SIZE = 12;

    private boolean init = false;
    
    //synth elements
    private Dac dac;
    private Oscillator oscA, oscB;
    private ExpEnv envA, envB;
    private Delay ugDelay;
    private Looper looper;

    // which finger ID corresponds to which instrument
    private int fingerA = -1;
    private int fingerB = -1;
    private int fingerC = -1;

    private SensorManager sensorManager = null;
    MediaPlayer secretSauce;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Brewing sauce...");

        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        secretSauce = MediaPlayer.create(this, R.raw.sauceboss);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        view = (SauceView)findViewById(R.id.sauceview);
        view.setOnTouchListener(this);
        
        Thread t = new Thread() {
      	  public void run() {
      	    try {
              //default instruments chosen here
              oscA = new SingingSaw();
              oscB = new Sine();
      	    	envA = new ExpEnv();
              envB = new ExpEnv();
      	    	dac = new Dac();
      	    	ugDelay = new Delay(delayRate);
      	    	looper = new Looper();

      	    	looper.chuck(dac);
      	    	ugDelay.chuck(looper);

      	    	envA.chuck(ugDelay);
              envB.chuck(ugDelay);

              //TODO these should get chucked to different envelopes but it seems to cause issues
      	    	oscA.chuck(envA);
      	    	oscB.chuck(envA);

      	    	envA.setFactor(ExpEnv.medFactor);
      	    	envA.setActive(true);
      	    	//envB.setFactor(ExpEnv.medFactor);
      	    	//envB.setActive(true);
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
      oscA.setBaseFreq(newFreq);
      oscB.setBaseFreq(newFreq);

      if (delayRate == 0) {
        ugDelay.updateRate(1); //i'm a hack. FIXME rate of 0 doesn't work
      } else {
        ugDelay.updateRate(delayRate);
      }
      
      oscA.setLag(lag / 100f);
      oscB.setLag(lag / 100f);
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

    /**
     * That main goodness. Handles touch events and gets properties of them to change the oscillators
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (! init) return false;

      int maxHeight = v.getMeasuredHeight();
      int maxWidth = v.getMeasuredWidth();

      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      
      if (actionCode == MotionEvent.ACTION_UP && dac.isPlaying()) { //last finger lifted. stop playback
        oscA.stop();
        oscB.stop();
        
        fingerA = -1;
        fingerB = -1;
        fingerC = -1;

        view.clearFingers();
        return true;
      }

      int pointerCount = event.getPointerCount();
      if (pointerCount == 5) {
        secretSauce.start(); //the secret sauce
        return true;
      }
    
      /*
       * Loop through each finger.
       * We go backwards because of the buttons. If a button is pressed and held, the next finger call
       * will be a POINTER_DOWN call, but the first button will interpret it first since it has a smaller index. That's bad.
       */
      for (int i = pointerCount - 1; i > -1; i--) {
        int id = event.getPointerId(i);
        float y = event.getY(i);
        float x = event.getX(i);

        //which osc does this finger correspond to?
        if (view.isInPad(x,y)) {
          Oscillator osc = null;
          if (id == fingerA || fingerA == -1) {
            osc = this.oscA;
            fingerA = id;
          } else if (id == fingerB || fingerB == -1) {
            osc = this.oscB;
            fingerB = id;
          } else if (fingerC == -1) {
          	fingerC = id;
          }

          int controllerWidth = (int) (maxWidth * SauceView.controllerWidth); 

          if (osc != null) { 
          	//finger down
            if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
              view.updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
              Log.i(TAG,"pad pointer finger :" + ((int)((maxHeight - y) / maxHeight * TRACKPAD_GRID_SIZE)));
  
              //play if we were stopped
              if(! osc.isPlaying())
                osc.togglePlayback();
              
              updateFrequency(id, (int)((maxHeight - y) / maxHeight * TRACKPAD_GRID_SIZE));
              updateAmplitude(id, (x - controllerWidth) / (maxWidth - controllerWidth));
            } else {
              //finger up. kill the osc
              final int upId = event.getActionIndex();
              Log.d(TAG, upId + " lifted");
              Oscillator upOsc;
              if (upId == fingerA) {
              	upOsc = this.oscA;
              	fingerA = -1;
              } else if (upId == fingerB) {
              	upOsc = this.oscB;
              	fingerB = -1;
              } else {
              	return false;
              }
              
              view.removeFinger(upId);
  
              if(upOsc.isPlaying())
                upOsc.togglePlayback();
            }
          } else if (id == fingerC) {
//            final int upId = event.getActionIndex();
            if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE)
              view.updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
//            else
//              view.removeFinger(upId);

            oscA.setModRate((int)((x - controllerWidth) / (maxWidth - controllerWidth) * MOD_RATE_MAX));
            oscA.setModDepth((int)((maxHeight - y) / maxHeight * MOD_DEPTH_MAX));
            oscB.setModRate((int)((x - controllerWidth) / (maxWidth - controllerWidth) * MOD_RATE_MAX));
            oscB.setModDepth((int)((maxHeight - y) / maxHeight * MOD_DEPTH_MAX));          	
          }
        } else {
          //controller buttons
          //TODO FIXME there needs to be a more abstracted way to do these
        	if (y <= maxHeight / SauceView.numButtons) {
            //toggle loop. only because it's the first button. bleg
            final int upId = event.getActionIndex();
            if ((actionCode == MotionEvent.ACTION_POINTER_DOWN &&
                upId != fingerA &&
                upId != fingerB) || actionCode == MotionEvent.ACTION_DOWN) {
              boolean isRecording = looper.toggleRecording();
            	Log.i(TAG,"action down :" + (isRecording));
              
              if (isRecording)
              	view.focusLooper();
              else
              	view.unfocusLooper();
                  
            }
          } else {
          	//reset looper
            final int upId = event.getActionIndex();
            if (((actionCode == MotionEvent.ACTION_POINTER_DOWN || 
                actionCode == MotionEvent.ACTION_POINTER_UP) &&
                upId != fingerA &&
                upId != fingerB) || actionCode == MotionEvent.ACTION_DOWN) {
            	looper.reset();
            	view.unfocusLooper();
            }          	
          }
        }
      }

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
            view.setVisuals(extras.getBoolean("visuals"));
            updateSettings();
          }
        }
    }
   
    private boolean launchSettings() {
    	Intent intent = new Intent(SauceEngine.this, Settings.class);
    	intent.putExtra("octave", octave);
    	intent.putExtra("note", note);
    	intent.putExtra("file name", fileName);
    	intent.putExtra("delay rate", delayRate);
    	intent.putExtra("lag", lag);
    	intent.putExtra("visuals", view.getVisuals());
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

      Oscillator oldOsc;
      if (oscNum == 0) {
        oldOsc = this.oscA;
        this.oscA.unchuck(envA);        
      } else if (oscNum == 1) {
        oldOsc = this.oscB;
        this.oscB.unchuck(envB);
      } else {
      	return false;
      }
      //TODO destroy old maybe? make sure it gets garbage collected

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
        this.oscA = oldOsc;
        this.oscA.chuck(envA);
      } else if (oscNum == 1) {
        this.oscB = oldOsc;
        this.oscB.chuck(envB);
      }
    	
    	return false;
    }
    
    public void updateAmplitude(int key, float amp) {
      Oscillator osc = null;
      if (key == fingerA) 
        osc = this.oscA;
      else if (key == fingerB)
        osc = this.oscB;

      if (osc != null)
        osc.setAmplitude(amp);
    }      
    public void updateFrequency(int key, int offset) {
      Oscillator osc = null;
      if (key == fingerA) 
        osc = this.oscA;
      else if (key == fingerB)
        osc = this.oscB;

      if (osc != null)
        osc.setFreqByOffset(scale, offset);
    }
}
