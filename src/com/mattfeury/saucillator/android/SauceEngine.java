package com.mattfeury.saucillator.android;

import java.util.ArrayList;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Toast;

/*
 * Main activity for the App. This will spin up the visuals and the audio engine. 
 * The audio engine gets its own thread.
 */
public class SauceEngine extends Activity implements OnTouchListener {
    private static final String TAG = "Sauce";
    private SauceView view;

    public enum Modes {
      EDIT {
        public String toString() {
          return "Edit Instrument";
        }
      },
      PLAY_MULTI {
        public String toString() {
          return "Multi Instrument";
        }
      }
    }
    private Modes mode = Modes.EDIT;

    //defaults
    private int delayRate = UGen.SAMPLE_RATE / 4;
    private int lag = (int)(DEFAULT_LAG * 100);
    private int note = 0;
    private int octave = 4;

    //music shtuffs
    public int[] scale = Theory.pentatonicScale;

    public final static int MOD_RATE_MAX = 20;
    public final static int MOD_DEPTH_MAX = 1000;
    public final static float DEFAULT_LAG = 0.5f;
    public static int TRACKPAD_GRID_SIZE = 12;

    private boolean init = false;
    
    // which finger ID corresponds to which instrument
    // TODO maybe make "Fingerable" interface... lolol
    private final int maxFingers = 5;
    private Object[] fingersById = new Object[maxFingers];

    //synth elements
    private Dac dac;
    private Delay ugDelay;
    private Looper looper;
    private ParametricEQ eq;
    private Oscillator[] oscillatorsById = new Oscillator[maxFingers];

    MediaPlayer secretSauce;
    private Vibrator vibrator;
    private SubMenu instrumentMenu;
    private final int instrumentMenuId = 9;
    private boolean canVibrate = false;
    private int VIBRATE_SPEED = 100; //in ms
    public static final String DATA_FOLDER = "/sdcard/sauce/";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Brewing sauce...");

        super.onCreate(savedInstanceState);

        secretSauce = MediaPlayer.create(this, R.raw.sauceboss);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        view = (SauceView)findViewById(R.id.sauceview);
        view.setOnTouchListener(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        if (vibrator != null)
          canVibrate = true;
        
        Thread t = new Thread() {
      	  public void run() {
      	    try {
      	    	dac = new Dac();
      	    	ugDelay = new Delay(delayRate);
      	    	looper = new Looper();

      	    	eq = new ParametricEQ();
      	    	eq.chuck(dac);
      	    	looper.chuck(eq);
      	    	ugDelay.chuck(looper);

      	    	dac.open();
              init = true;
              setupParamHandlers();
              Log.i(TAG, "Sauce ready.");

      	      while (true) {
        	    	dac.tick();
      	      }
      	    }
      	    catch(Exception ex) {
      	    	ex.printStackTrace();
      	    	Log.e(TAG, "bad time " + ex.toString());
      	    	dac.close();
      	    }
      	  }
      	};
      	
      t.start();
    }

    protected void onDestroy() {
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    // Maintains landscape mode
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public boolean isFingered(Object obj) {
      for (int i = 0; i < fingersById.length; i++)
        if (obj.equals(fingersById[i]))
          return true;

      return false;
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
        for (int i = 0; i < fingersById.length; i++) {
          Oscillator osc = oscillatorsById[i];
          if (osc != null && osc.isPlaying() && ! osc.isReleasing())
            osc.togglePlayback();

          fingersById[i] = null;
        }

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

        final int actionIndex = event.getActionIndex();
        final int actionId = event.getPointerId(actionIndex);

        // Finger on main pad. This affects an oscillator or parameter
        if (view.isInPad(x,y)) {
          int controllerWidth = (int) (maxWidth * SauceView.controllerWidth);
          float yInverted = maxHeight - y;
          float xScaled = (x - controllerWidth) / (maxWidth - controllerWidth);
          float yScaled = yInverted / maxHeight;

          Object controlled = fingersById[id];
          boolean fingerDefined = controlled != null;
          
          if (mode == Modes.EDIT) {
            // Determine if this edits a parameter. otherwise, edit the oscillator
            DrawableParameter param = view.optParameter(x, y, controlled);

            // Modify the osc (stored as id = 0)
            // TODO fail nicely if this doesn't exist. alert the user to choose an instrument
            Oscillator osc = getOrCreateOscillator(0);

            // If this is on a parameter AND
            // this finger isn't controlling something or it's controlling this param)
            if (param != null && (! fingerDefined || param.equals(controlled))) {
              // Modify the parameter
              if (! fingerDefined)
                fingersById[id] = param;

              param.set(xScaled, yScaled);
              view.invalidate();
            } else if (osc != null && (osc.equals(controlled) || (! fingerDefined && ! isFingered(osc)))) {
              fingersById[id] = osc;

              if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
                view.updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));

                //play if we were stopped
                if(! osc.isPlaying())
                  osc.togglePlayback();
                else if (osc.isReleasing())
                  osc.startAttack();

                osc.setFreqByOffset(scale, (int)(yScaled * TRACKPAD_GRID_SIZE));
                osc.setAmplitude(xScaled);
              } else if (osc != null && actionCode == MotionEvent.ACTION_POINTER_UP && actionIndex == i) {
                //finger up. kill the osc 
                view.removeFinger(id);
    
                if(osc.isPlaying())
                  osc.togglePlayback();
                else if (osc.isAttacking())
                  osc.startRelease();
              } 
            }
          } else if (mode == Modes.PLAY_MULTI) {

            // Determine which synth this finger corresponds to
            Oscillator osc = getOrCreateOscillator(id);

            if (! fingerDefined)
              fingersById[id] = osc;

            if (osc != null) {
              //finger down
              if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
                view.updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
    
                //play if we were stopped
                if(! osc.isPlaying())
                  osc.togglePlayback();
                else if (osc.isReleasing())
                  osc.startAttack();
              
                updateFrequency(id, (int)(yScaled * TRACKPAD_GRID_SIZE));
                updateAmplitude(id, xScaled);
              } else if (actionIndex == i) {
                //finger up. kill the osc
                view.removeFinger(i);
                
                if(osc.isPlaying())
                  osc.togglePlayback();
                else if (osc.isAttacking())
                  osc.startRelease();  
              }
            }
            
          }

          if (actionCode == MotionEvent.ACTION_POINTER_UP) {
            fingersById[actionId] = null;
          }
        } else {
          //controller buttons
          //TODO FIXME there needs to be a more abstracted way to do these

          int buttonHeight = maxHeight / SauceView.numButtons;

          final int upIndex = event.getActionIndex();
          if ((actionCode == MotionEvent.ACTION_POINTER_DOWN && upIndex == i)
              || actionCode == MotionEvent.ACTION_DOWN) {

            if (canVibrate)
              vibrator.vibrate(VIBRATE_SPEED);

          	if (y <= buttonHeight) {
              //Toggle Looper Button
              boolean isRecording = looper.toggleRecording();
              if (isRecording)
              	view.focusLooper();
              else
              	view.unfocusLooper();

            } else if (y <= buttonHeight * 2) {
              //Undo Looper Button
              looper.undo();
              view.unfocusLooper();
            } else {
              //Reset Looper Button
              looper.reset();
              view.unfocusLooper();
            }
          }
        }
      }

      return true; // indicate event was handled
    }

    private void setupParamHandlers() {
      // TODO is this always edit mode?      
      Oscillator osc = getOrCreateOscillator(0);
      
      DrawableParameter eqParam = new DrawableParameter(
            new ParameterHandler() {
              public void updateParameter(float x, float y) {
                //FIXME yuck
                x = Utilities.unscale(x, SauceView.controllerWidth, 1);

                eq.setFrequency(x * x);
                eq.setQ(y * y);
                Log.i(TAG, "NEW NEW: " + x*x + " / new q: " + y*y);
              }
            },
            eq.getFrequency(), //frequency on x
            eq.getQ() // q on y
          );

      DrawableParameter lfoParam = new DrawableParameter(
            new ParameterHandler() {
              public void updateParameter(float x, float y) {
                //FIXME yuck
                x = Utilities.unscale(x, SauceView.controllerWidth, 1);

                Oscillator osc = getOrCreateOscillator(0);

                osc.setModRate((int)(x * MOD_RATE_MAX));
                osc.setModDepth((int)(y * MOD_DEPTH_MAX));
                
                Log.i(TAG, "LFO rate : " + x + " / depth: " + y);
              }
            },
            osc.getModRate() / (float)MOD_RATE_MAX, // mod rate on x
            osc.getModRate() / (float)MOD_DEPTH_MAX // mod depth on y
          );
      
      view.addParam(eqParam);
      view.addParam(lfoParam);
    }

    /**
     * Oscillator handlers
     */
    public Oscillator optOscillator(int id) {
      return oscillatorsById[id];
    }
    public Oscillator getOrCreateOscillator(int id) {
      Oscillator osc = oscillatorsById[id];
      if (osc != null)
        return osc;

      //TODO have this lookup in a map or something that can be changed by a UI
      osc = InstrumentManager.getInstrument(getAssets(), "Sine");
      connectOsc(osc);
      oscillatorsById[id] = osc;
      
      return osc;
    }
    public void updateAmplitude(int id, float amp) {
      Oscillator osc = optOscillator(id);

      if (osc != null)
        osc.setAmplitude(amp);
    }      
    public void updateFrequency(int id, int offset) {
      Oscillator osc = optOscillator(id);

      if (osc != null)
        osc.setFreqByOffset(scale, offset);
    }

    // Update oscillators based on the settings parameters.
    private void updateOscSettings() {
      float newFreq = Theory.getFrequencyForNote(note + 1, octave);
      for (Oscillator osc : oscillatorsById)
        if (osc != null)
          osc.setBaseFreq(newFreq);

      //TODO FIXME  move these to be specified in the .sauce files
      if (delayRate == 0) {
        ugDelay.updateRate(1); //i'm a hack. FIXME rate of 0 doesn't work
      } else {
        ugDelay.updateRate(delayRate);
      }
    }
    
    /**
     * Settings handlers
     */
    private boolean launchSettings() {
    	Intent intent = new Intent(SauceEngine.this, Settings.class);
    	intent.putExtra("octave", octave);
    	intent.putExtra("note", note);
    	intent.putExtra("file name", WavWriter.filePrefix);
    	intent.putExtra("delay rate", delayRate);
    	intent.putExtra("lag", lag);
    	intent.putExtra("visuals", view.getVisuals());
    	startActivityForResult(intent, 0);
    	return true;
    }
    // Called when settings activity ends. Updates proper params
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
          updateOscSettings();
        }
      }
    }

    /**
     * Menu handlers
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);

      // FIXME this is vomit-inducing
      // Make sure that instrument selector is first item in menu
      instrumentMenu = menu.getItem(0).getSubMenu();

      // Set defaults
      MenuItem toggle = menu.findItem(R.id.toggleMode);
      if (mode == Modes.PLAY_MULTI) {
        toggle.setTitle(Modes.EDIT.toString());
      } else {
        toggle.setTitle(Modes.PLAY_MULTI.toString());
      }

      return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
      boolean result = super.onPrepareOptionsMenu(menu);

      // TODO lookup all instruments
      instrumentMenu.clear();

      // TODO set checked based on current
      ArrayList<String> instruments = InstrumentManager.getAllInstrumentNames(getAssets());
      String[] names = instruments.toArray(new String[0]);
      int i = 0;
      for (String name : names) {
        instrumentMenu.add(instrumentMenuId, i, i, name);
        i++;
      }

      instrumentMenu.setGroupCheckable(instrumentMenuId, true, true);
      
      return result;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch (item.getGroupId()) {
    		case R.id.scales:
    			return scaleSelection(item);
        case instrumentMenuId:
          return instrumentSelection(item, 0); //TODO pass in an id for multi mode
    		default:
          android.util.Log.d("no group id", ""+item.getGroupId());          
    	}
    	switch (item.getItemId()) {
    		case R.id.quit:
    			onDestroy();
    			return true;
    		case R.id.settings:
    			return launchSettings();
    		case R.id.record:
    			return record(item);
        case R.id.toggleMode:
          toggleMode(item);
          return true;
    		default:
    	}
      return false;
    }

    private Modes toggleMode(MenuItem item) {
      Modes other = mode;
      if (mode == Modes.PLAY_MULTI) {
        mode = Modes.EDIT;
      } else {
        mode = Modes.PLAY_MULTI;
      }

      item.setTitle(other.toString());
      Toast.makeText(this, "Switched to " + mode + " Mode.", Toast.LENGTH_SHORT).show();
      return mode;
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
          scale = Theory.pentatonicScale;
          break;
        case R.id.major: //major
          scale = Theory.majorScale;
          break;
        case R.id.minor: //minor
          scale = Theory.minorScale;
          break;
        case R.id.blues: //blues
          scale = Theory.minorBluesScale;
          break;
        case R.id.chromatic: //chromatic
          scale = Theory.chromaticScale;
          break;
        default:
      }
    	return false;
    }

    private void connectOsc(Oscillator osc) {
      osc.chuck(ugDelay);
    }
    private void disconnectOsc(Oscillator osc) {
      osc.unchuck(ugDelay);
    }
    private boolean instrumentSelection(MenuItem item, int id) {
    	if (item.isChecked())
    		return true;

      String name = (String) item.getTitle();
      ComplexOsc newOsc = InstrumentManager.getInstrument(getAssets(), name);

      if (newOsc == null) {
        Toast.makeText(this, "Bad Instrument.", Toast.LENGTH_SHORT).show();
        return false;
      }

      if (oscillatorsById[id] != null)
        disconnectOsc(oscillatorsById[id]);

      connectOsc(newOsc);
      oscillatorsById[id] = newOsc;

      //FIXME this is a hack for now so that the instrument specific settings (lag & base freq)
      //FIXME get reset on every instrument. We should abstract as InstrumentSettings and pass them around that way.
      updateOscSettings();

      return true;
    }
}
