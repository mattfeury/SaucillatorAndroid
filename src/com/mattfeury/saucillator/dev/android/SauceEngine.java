package com.mattfeury.saucillator.dev.android;

import java.util.concurrent.ConcurrentHashMap;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.instruments.*;
import com.mattfeury.saucillator.dev.android.sound.*;
import com.mattfeury.saucillator.dev.android.tabs.*;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.*;
import com.mattfeury.saucillator.dev.android.services.*;
import com.mattfeury.saucillator.dev.android.settings.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;

/*
 * Main activity for the App. This class has two main purposes:
 * 
 * 1. Handle user input.
 * 2. Delegate events (based user input or otherwise) between the AudioEngine and the SauceView.
 * 
 * It also currently handles all the Activity necessities: menus, dialogs, etc.
 */
public class SauceEngine extends Activity implements OnTouchListener {
    public static final String TAG = "Sauce";

    //defaults
    public static int TRACKPAD_GRID_SIZE = 12;
    public final static int TRACKPAD_SIZE_MAX = 16, TRACKPAD_SIZE_MIN = 4;

    private boolean init = false;

    // which finger ID corresponds to which fingerable layout element. e.g. buttons, knobs, etc.
    private ConcurrentHashMap<Integer, Fingerable> fingersById = new ConcurrentHashMap<Integer, Fingerable>();

    public static final String DATA_FOLDER = "sauce/";

    private static final String tutorialName = "buffalo.0";
    
    private static final int TUTORIAL_DIALOG = 0;

    private Object mutex = new Object();

    private SauceView view;
    private TabManager tabManager;
    private AudioEngine audioEngine;

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

      VibratorService.setup((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
      ActivityService.setup(this);
      InstrumentService.setup(getAssets());

      this.audioEngine = new AudioEngine(this, mutex);

      // We wait until the dac is spun up to create the param handlers since
      // they require certain DAC elements (e.g. EQ). We can't do it in the DAC thread
      // because only the thread that spawned the view can redraw it.
      synchronized(mutex) {
        try {
          if (! init) {
            mutex.wait();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        this.tabManager = new TabManager();

        view.addDrawable(tabManager);

        tabManager.addTab(new FxTab(audioEngine));
        tabManager.addTab(new InstrumentManagerTab(audioEngine));
        tabManager.addTab(new TimbreTab(audioEngine));
        tabManager.addTab(new LooperTab(audioEngine));
        tabManager.addTab(new EqTab(audioEngine));
        tabManager.addTab(new PadTab(audioEngine));
        tabManager.addTab(new RecorderTab(audioEngine));
      }
    }

    /**
     * Called once, generally shortly after onCreate.
     */
    public void audioInitialized() {
      init = true;
    }

    protected Dialog onCreateDialog(int id){
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      switch(id) {
        case TUTORIAL_DIALOG:
          builder
            .setTitle("Saucillator 2.0 Buffalo (beta)")
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
    public void onPause() {
        super.onPause();

        // When the audio thread keeps running in the background, it gets put on low priority
        // which results in pops on the AudioTrack. this should stop it ticking in the meantime. 
        if (! audioEngine.isLooping()) {
            audioEngine.pauseDac();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        audioEngine.playDac();
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

      //int maxHeight = v.getMeasuredHeight();
      //int maxWidth = v.getMeasuredWidth();

      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      
      if (actionCode == MotionEvent.ACTION_UP && audioEngine.isPlaying()) { //last finger lifted. stop playback
        fingersById.clear();
        audioEngine.stopAllOscillators();
        view.clearFingers();

        return true;
      }

      int pointerCount = event.getPointerCount();
      final int actionIndex = event.getActionIndex();
      final int actionId = event.getPointerId(actionIndex);

      /*
       * Loop through each finger.
       * We go backwards because of the buttons. If a button is pressed and held, the next finger call
       * will be a POINTER_DOWN call, but the first button will interpret it first since it has a smaller index. That's bad.
       */
      for (int i = pointerCount - 1; i > -1; i--) {
        final int id = event.getPointerId(i);
        if (id < 0)
          continue;

        float y = event.getY(i);
        float x = event.getX(i);

        Fingerable controlled = fingersById.get(id);

        if (controlled != null) {
          Box<Fingerable> fingered = controlled.handleTouch(id, event);

          // It may return empty if it no longer wishes to handle touches
          if (! fingered.isDefined()) {
            fingersById.remove(id);

            if (controlled instanceof Drawable) {
              view.removeDrawable(((Drawable)controlled));
            }
          }
        } else if (view.isInPad(x,y)) {
          handleTouchForOscillator(id, event);
        } else {
          handleTouchForController(id, event);
        }
        view.invalidate();
      }

      if (actionCode == MotionEvent.ACTION_POINTER_UP) {
        fingersById.remove(actionId);
      }

      return true; // indicate event was handled
    }

    private void handleTouchForOscillator(int id, MotionEvent event) {
      ComplexOsc osc = audioEngine.getOrCreateOscillator(id);
      Fingerable controlled = fingersById.get(id);
      boolean fingerDefined = controlled != null;

      if (osc == null || (! osc.equals(controlled) && (fingerDefined || isFingered(osc)))) return;

      final int index = event.findPointerIndex(id);
      final int y = (int) event.getY(index);
      final int x = (int) event.getX(index);

      FingeredOscillator fingerableOsc = new FingeredOscillator(view, osc, x, y);
      fingersById.put(id, fingerableOsc);
      view.addDrawable(fingerableOsc);

      fingerableOsc.handleTouch(id, event);
    }
    
    private void handleTouchForController(final int id, MotionEvent event) {
      Fingerable controlled = fingersById.get(id);

      if (controlled == null) {
        Box<Fingerable> fingered = tabManager.handleTouch(id, event);
        fingered.foreach(new EachFunc<Fingerable>() {
          public void func(Fingerable k) {
            fingersById.put((Integer)id, k);
          }            
        });
      } else {
        controlled.handleTouch(id, event);
      }

      view.invalidate();
    }

    public boolean isFingered(Object obj) {
      return (obj != null && fingersById.containsValue(obj));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);
      return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
        case R.id.settings:
          Intent intent = new Intent(SauceEngine.this, Settings.class);
          startActivityForResult(intent, 0);
          return true;
        case R.id.help:
          showDialog(TUTORIAL_DIALOG);
          return true;
        case R.id.quit:
          onDestroy();
          return true;
      }
      return false;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
