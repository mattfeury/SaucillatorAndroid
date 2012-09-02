package com.mattfeury.saucillator.dev.android.sound;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.mattfeury.saucillator.dev.android.R;
import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.InstrumentManager;
import com.mattfeury.saucillator.dev.android.instruments.Theory;
import com.mattfeury.saucillator.dev.android.instruments.Theory.Scale;

public class AudioEngine {
  private int note = 0;
  private int octave = 4;
  private String scaleId = Scale.PENTATONIC.toString();
  public int[] scale = Theory.pentatonicScale;

  public final static int DELAY_MAX = UGen.SAMPLE_RATE; //Is this right?
  public final static int MOD_RATE_MAX = 20;
  public final static int MOD_DEPTH_MAX = 1000;

  public final static float DEFAULT_LAG = 0.5f;

  //synth elements
  private Dac dac;
  private Looper looper;
  private ParametricEQ eq;
  private ConcurrentHashMap<Integer, ComplexOsc> oscillatorsById = new ConcurrentHashMap<Integer, ComplexOsc>();

  private SauceEngine sauceEngine;

  // The currentOscillator is never actually heard
  // It is kept as a template and updated anytime an instrument is edited/created
  // We create a deepCopy of it for actually playing.
  private static ComplexOsc currentOscillator;

  //MediaPlayer secretSauce;

  public AudioEngine(SauceEngine sauce, final Object mutex) {
    this.sauceEngine = sauce;
    
    //secretSauce = MediaPlayer.create(this, R.raw.sauceboss);

    //Default
    currentOscillator = InstrumentManager.getInstrument(sauceEngine.getAssets(), "Sine");
    
    Thread t = new Thread() {
      public void run() {
        try {
          synchronized(mutex) {
            dac = new Dac();
            looper = new Looper();

            eq = new ParametricEQ();
            eq.chuck(dac);
            looper.chuck(eq);

            dac.open();
            Log.i(SauceEngine.TAG, "Sauce ready.");
            mutex.notify();
          }

          while (true) {
            dac.tick();
          }
        }
        catch(Exception ex) {
          ex.printStackTrace();
          Log.e(SauceEngine.TAG, "bad time " + ex.toString());
          dac.close();
          mutex.notify();
        }
      }
    };
    
    t.start();
  }
  
  public boolean isPlaying() {
    return dac.isPlaying();
  }

  public void stopAllOscillators() {
    Set<Entry<Integer, ComplexOsc>> oscs = oscillatorsById.entrySet();
    for (Entry<Integer, ComplexOsc> oscEntry : oscs) {
      ComplexOsc osc = oscEntry.getValue();
      if (osc != null && osc.isPlaying() && ! osc.isReleasing())
        osc.togglePlayback();
    }
  }
  
  public static ComplexOsc getCurrentOscillator() {
    return currentOscillator;
  }
  public void resetOscillators() {
    Set<Integer> oscIds = oscillatorsById.keySet();
    for (Integer oscId : oscIds) {
      ComplexOsc osc = oscillatorsById.get(oscId);
      if (osc != null)
        disconnectOsc(osc);

      oscillatorsById.remove(oscId);
    }

  }
  public ComplexOsc optOscillator(int id) {
    return oscillatorsById.get(id);
  }
  public ComplexOsc getOrCreateOscillator(int id) {
    ComplexOsc osc = oscillatorsById.get(id);
    if (osc != null)
      return osc;

    ComplexOsc copy = InstrumentManager.copyInstrument(sauceEngine.getAssets(), currentOscillator);
    if (copy != null)
      osc = copy;
    else {
      osc = InstrumentManager.getInstrument(sauceEngine.getAssets(), "Sine");
      Toast.makeText(sauceEngine, "Error: Unable to duplicate instrument", Toast.LENGTH_SHORT).show();
    }

    connectOsc(osc);
    oscillatorsById.put(id, osc);

    // Ensure new oscillator has up-to-date settings
    updateOscSettings();

    return osc;
  }
  private void connectOsc(ComplexOsc osc) {
    osc.chuck(looper);
  }
  private void disconnectOsc(ComplexOsc osc) {
    osc.unchuck(looper);
  }
  public void updateAmplitude(int id, float amp) {
    ComplexOsc osc = optOscillator(id);

    if (osc != null)
      osc.setAmplitude(amp);
  }      
  public void updateFrequency(int id, int offset) {
    ComplexOsc osc = optOscillator(id);

    if (osc != null)
      osc.setFreqByOffset(scale, offset);
  }
  // Update oscillators based on the settings parameters.
  private void updateOscSettings() {
    float newFreq = Theory.getFrequencyForNote(note + 1, octave);
    Collection<ComplexOsc> oscs = oscillatorsById.values();
    for (ComplexOsc osc : oscs)
      if (osc != null)
        osc.setBaseFreq(newFreq);
  }

  public void setOscillator(ComplexOsc newOsc) {
    currentOscillator = newOsc;
    resetOscillators();

    connectOsc(newOsc);
    oscillatorsById.put(0, newOsc);

    updateOscSettings();    
  }

  public void setScaleById(String scaleId) {
    this.scaleId = scaleId;

    if (scaleId.equals(Scale.PENTATONIC.toString())) {
      scale = Theory.pentatonicScale;
    } else if (scaleId.equals(Scale.MAJOR.toString())) {
      scale = Theory.majorScale;
    } else if (scaleId.equals(Scale.MINOR.toString())) {
      scale = Theory.minorScale;
    } else if (scaleId.equals(Scale.MINOR_BLUES.toString())) {
      scale = Theory.minorBluesScale;
    } else {
      scale = Theory.chromaticScale;
    }
  }

  public void record() {
    boolean isRecording = dac.toggleRecording();
    if (isRecording) {
      //item.setTitle("Stop Recording");
      //item.setIcon(R.drawable.ic_grey_rec);
      Toast.makeText(sauceEngine, "Recording.", Toast.LENGTH_SHORT).show();
    }
    else {
      //item.setTitle("Record");
      //item.setIcon(R.drawable.ic_rec);

      File saved = WavWriter.getLastFile();
      if(saved == null) {
        Toast.makeText(sauceEngine, "Stopped Recording. File could not be saved. I blew it.", Toast.LENGTH_SHORT).show();
        return;
      } else {
        Toast.makeText(sauceEngine, "Stopped Recording. File saved at: " + saved.getAbsolutePath(), Toast.LENGTH_LONG).show();
      }

      Intent intent = new Intent(Intent.ACTION_SEND).setType("audio/*");
      intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saved));
      sauceEngine.startActivity(Intent.createChooser(intent, "Share to"));
    }
  }
}
