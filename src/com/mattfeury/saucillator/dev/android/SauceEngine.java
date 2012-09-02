package com.mattfeury.saucillator.dev.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.instruments.*;
import com.mattfeury.saucillator.dev.android.instruments.Theory.Scale;
import com.mattfeury.saucillator.dev.android.settings.ModifyInstrument;
import com.mattfeury.saucillator.dev.android.settings.Settings;
import com.mattfeury.saucillator.dev.android.sound.*;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.visuals.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
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
    public static final String TAG = "Sauce";
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
    private Modes mode = Modes.PLAY_MULTI;

    //defaults
    public static int TRACKPAD_GRID_SIZE = 12;
    public final static int TRACKPAD_SIZE_MAX = 16;

    private boolean init = false;

    // which finger ID corresponds to which instrument
    // maybe make "Fingerable" interface... lolol
    private ConcurrentHashMap<Integer, Object> fingersById = new ConcurrentHashMap<Integer, Object>();

    private Vibrator vibrator;
    private boolean canVibrate = false;
    private int VIBRATE_SPEED = 100; //in ms
    private SubMenu instrumentMenu;
    private final int instrumentMenuId = 9;
    public static final String DATA_FOLDER = "sauce/";
    public static final int MODIFY_ACTION = 1;

    private static final String tutorialName = "showAlfredoTutorial";
    
    private static final int BACKPRESS_DIALOG = 0,
                             TUTORIAL_DIALOG = 1;

    private Object mutex = new Object();

    private AudioEngine audioEngine;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
      Log.i(TAG, "Brewing sauce...");
      super.onCreate(savedInstanceState);

      // Show tutorial on first load
      SharedPreferences prefs = getPreferences(MODE_PRIVATE);
      boolean shouldShowTutorial = prefs.getBoolean(tutorialName, true);
      if (shouldShowTutorial) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(tutorialName, false);
        editor.commit();

        showDialog(TUTORIAL_DIALOG);
      }

      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.main);
      view = (SauceView)findViewById(R.id.sauceview);
      view.setOnTouchListener(this);

      vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

      if (vibrator != null)
        canVibrate = true;
      
      this.audioEngine = new AudioEngine(this, mutex);

      // We wait until the dac is spun up to create the param handlers since
      // they require certain DAC elements (e.g. EQ). We can't do it in the DAC thread
      // because only the thread that spawned the view can redraw it.
      synchronized(mutex) {
        try {
          if (! init)
            mutex.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        init = true;

        // TODO setup visual layout that depends on audio shtuff
      }
    }

    protected Dialog onCreateDialog(int id){
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      switch(id) {
        case BACKPRESS_DIALOG:
          builder
            .setTitle("Exit or hide?")
            .setMessage("Should the app stay awake and keep playing music? Keeping the app playing in the background may cause popping.")
            .setCancelable(true)
            .setPositiveButton("Quit",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    SauceEngine.this.finish();
                  }
            })
            .setNegativeButton("Hide",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    moveTaskToBack(true);
                  }
            });
          break;
        case TUTORIAL_DIALOG:
          builder
            .setTitle("Saucillator 1.0 Alfredo")
            .setView(LayoutInflater.from(this).inflate(R.layout.tutorial_dialog,null))
            .setCancelable(false)
            .setNeutralButton("Good Juice. Let's Sauce.", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
            }});
          break;
        default:
      }
      AlertDialog alert = builder.create();
      return alert;
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
      
      if (actionCode == MotionEvent.ACTION_UP && audioEngine.isPlaying()) { //last finger lifted. stop playback
        fingersById.clear();

        audioEngine.stopAllOscillators();
        view.clearFingers();
        return true;
      }

      int pointerCount = event.getPointerCount();

      /*
       * Loop through each finger.
       * We go backwards because of the buttons. If a button is pressed and held, the next finger call
       * will be a POINTER_DOWN call, but the first button will interpret it first since it has a smaller index. That's bad.
       */
      for (int i = pointerCount - 1; i > -1; i--) {
        int id = event.getPointerId(i);

        if (id < 0)
          continue;

        float y = event.getY(i);
        float x = event.getX(i);

        final int actionIndex = event.getActionIndex();
        final int actionId = event.getPointerId(actionIndex);

        // Finger on main pad. This affects an oscillator or parameter (when in edit mode)
        if (view.isInPad(x,y)) {
          float[] scaledCoords = view.scaleToPad(x,y);
          float xScaled = scaledCoords[0];
          float yScaled = scaledCoords[1];

          Object controlled = fingersById.get(id);
          boolean fingerDefined = controlled != null;

          // Determine if this edits a parameter. Otherwise, edit an osc, depending on mode.
          DrawableParameter param = view.optParameter(x, y, controlled);
          int oscId = mode == Modes.EDIT ? 0 : id;
          ComplexOsc osc = audioEngine.getOrCreateOscillator(oscId);

          // If this is on a parameter AND this finger isn't controlling something, OR
          // it's controlling this param
          if ((param != null && ! fingerDefined) || (controlled instanceof DrawableParameter)) {
            if (param != null && ! fingerDefined)
              fingersById.put(id, param);
            else
              param = (DrawableParameter) controlled;

            param.set(xScaled, yScaled);
            view.invalidate();
          } else if (osc != null && (osc.equals(controlled) || (! fingerDefined && ! isFingered(osc)))) {
            // Update oscillator
            if (! fingerDefined)
              fingersById.put(id, osc);

            handleTouchForOscillator(oscId, id, v, event);
          }

          if (actionCode == MotionEvent.ACTION_POINTER_UP) {
            fingersById.remove(actionId);
          }
        } else {
          // Buttons!
          // FIXME there needs to be a more abstracted way to do these

          final int upIndex = event.getActionIndex();
          if ((actionCode == MotionEvent.ACTION_POINTER_DOWN && upIndex == i)
              || actionCode == MotionEvent.ACTION_DOWN) {

            // Add a small margin to the right side to make accidental presses less frequent
            float controllerWidth = maxWidth * SauceView.controllerWidth;
            if (x < controllerWidth - (controllerWidth * .15f)) {
              if (canVibrate)
                vibrator.vibrate(VIBRATE_SPEED);

              int buttonHeight = maxHeight / SauceView.numButtons;          
              // Looper buttons
              if (y <= buttonHeight) {
                //Toggle Looper Button
                //boolean isRecording = looper.toggleRecording();
                /*if (isRecording)
                  view.focusLooper();
                else
                  view.unfocusLooper();*/
              } else if (y <= buttonHeight * 2) {
                //Undo Looper Button
                //looper.undo();
                view.unfocusLooper();
              } else {
                //Reset Looper Button
                //looper.reset();
                view.unfocusLooper();
              }
            } else if (x > maxWidth * SauceView.controllerWidth) {
              if (canVibrate)
                vibrator.vibrate(VIBRATE_SPEED);

              // Param toggler buttons
              view.toggleEnablerAt(x, y);
            }
          }
        }
      }

      return true; // indicate event was handled
    }

    /**
     * Oscillator handlers
     */
    public void handleTouchForOscillator(int oscId, int id, View v, MotionEvent event) {
      ComplexOsc osc = audioEngine.getOrCreateOscillator(oscId);

      if (osc == null) return;

      final int index = event.findPointerIndex(id);
      final int action = event.getAction();
      final int actionCode = action & MotionEvent.ACTION_MASK;
      final int actionIndex = event.getActionIndex();
      final int actionId = event.getPointerId(actionIndex);

      final float y = event.getY(index);
      final float x = event.getX(index);
      final float[] scaledCoords = view.scaleToPad(x,y);
      final float xScaled = scaledCoords[0];
      final float yScaled = scaledCoords[1];

      if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
        view.updateOrCreateFinger(id, event.getX(index), event.getY(index), event.getSize(index), event.getPressure(index));

        //play if we were stopped
        if(! osc.isPlaying())
          osc.togglePlayback();
        else if (osc.isReleasing())
          osc.startAttack();

        audioEngine.updateFrequency(oscId, (int)(yScaled * TRACKPAD_GRID_SIZE));
        audioEngine.updateAmplitude(oscId, xScaled);
      } else if (actionCode == MotionEvent.ACTION_POINTER_UP && actionId == id) {
        //finger up. kill the osc
        view.removeFinger(id);

        if(osc.isPlaying() && ! osc.isReleasing())
          osc.togglePlayback();
      }
      
    }

    public boolean isFingered(Object obj) {
      return (obj != null && fingersById.containsValue(obj));
    }


    /**
     * Settings handlers
     */
    private boolean launchSettings() {
    	Intent intent = new Intent(SauceEngine.this, Settings.class);
    	//intent.putExtra("octave", octave);
    	//intent.putExtra("note", note);
    	//intent.putExtra("file name", WavWriter.filePrefix);
    	//intent.putExtra("visuals", view.getVisuals());
      //intent.putExtra("scale", scaleId);
    	//startActivityForResult(intent, 0);
    	return true;
    }
    private void launchModifyInstrument(boolean create) {
    	Intent intent = new Intent(SauceEngine.this, ModifyInstrument.class);
    	intent.putExtra("createNew", create);
    	startActivityForResult(intent, SauceEngine.MODIFY_ACTION);
    }
    private void editInstrument() {
      launchModifyInstrument(false);
    }
    private void createInstrument() {
      launchModifyInstrument(true);
    }
    // Called when settings activity ends. Updates proper params
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      /*if (requestCode == 0 && data != null) {
        Bundle extras = data.getExtras();

        if (extras != null) {
          WavWriter.filePrefix = extras.getString("file name");
          note = extras.getInt("note");
          octave = extras.getInt("octave");
          scaleId = extras.getString("scale");

          view.setVisuals(extras.getBoolean("visuals"));
          selectScale(scaleId);
        }
      } else if (requestCode == SauceEngine.MODIFY_ACTION) {
        if (ModifyInstrument.modifying == null)
          return;

        currentOscillator = ModifyInstrument.modifying;
        resetOscillators();
        setupParamHandlers();
      }*/
      //updateOscSettings();
    }

    /**
     * Menu handlers
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);

      instrumentMenu = menu.findItem(R.id.selectInstrument).getSubMenu();

      return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
      boolean result = super.onPrepareOptionsMenu(menu);

      instrumentMenu.clear();

      ArrayList<String> instruments = InstrumentManager.getAllInstrumentNames(getAssets());
      String[] names = instruments.toArray(new String[0]);
      int i = 0;
      for (String name : names) {
        instrumentMenu.add(instrumentMenuId, i, i, name);
        i++;
      }

      instrumentMenu.setGroupCheckable(instrumentMenuId, false, true);
      
      return result;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getGroupId()) {
        case instrumentMenuId:
          return instrumentSelection(item);
    	}
    	switch (item.getItemId()) {
        case R.id.createInstrumentItem:
          createInstrument();
          return true;
        case R.id.editInstrumentItem:
          editInstrument();
          return true;
        case R.id.toggleMode:
          toggleMode(item);
          return true;
    		case R.id.settings:
    			return launchSettings();
    		case R.id.record:
    			return record(item);
        case R.id.quit:
          onDestroy();
          return true;
      }
      return false;
    }
    
    @Override
    public void onBackPressed() {
      showDialog(BACKPRESS_DIALOG);
    }

    private Modes toggleMode(MenuItem item) {
      if (mode == Modes.PLAY_MULTI) {
        mode = Modes.EDIT;
      } else {
        mode = Modes.PLAY_MULTI;
      }

      audioEngine.resetOscillators();
      Toast.makeText(this, "Switched to " + mode + " Mode.", Toast.LENGTH_SHORT).show();
      return mode;
    }

    private boolean record(MenuItem item) {
      audioEngine.record();
      return true;
    }

    private void selectScale(String scaleId) {
      audioEngine.setScaleById(scaleId);
    }

    private boolean instrumentSelection(MenuItem item) {
    	if (item.isChecked())
    		return true;

      String name = (String) item.getTitle();
      ComplexOsc newOsc = InstrumentManager.getInstrument(getAssets(), name);

      if (newOsc == null) {
        Toast.makeText(this, "Bad Instrument.", Toast.LENGTH_SHORT).show();
        return false;
      } else {
        audioEngine.setOscillator(newOsc);
        return true;
      }
    }
}
