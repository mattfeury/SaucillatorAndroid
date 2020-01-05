package com.mattfeury.saucillator.dev.android.sound;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.Theory;
import com.mattfeury.saucillator.dev.android.instruments.Theory.Scale;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.services.ActivityService;

public class AudioEngine {
  private int note = 0;
  private int octave = 4;
  //private String scaleId = Theory.allScales[0].toString();
  public static int[] scale = Theory.pentatonicScale;

  public final static int DELAY_RATE_MIN = 0, DELAY_RATE_MAX = UGen.SAMPLE_RATE; //Is this right?
  public final static int MOD_RATE_MIN = 0, MOD_RATE_MAX = 20;
  public final static int MOD_DEPTH_MIN = 0, MOD_DEPTH_MAX = 1000;

  public final static float DEFAULT_LAG = 0.5f; // i don't think this is used...

  private final static String defaultInstrument = "Starslide";

  // synth elements
  private Dac dac;
  private Looper looper;
  private ParametricEQ eq;
  private ConcurrentHashMap<Integer, ComplexOsc> oscillatorsById = new ConcurrentHashMap<Integer, ComplexOsc>();

  private SauceEngine sauceEngine;

  private DacThread thread;

  // The currentOscillator is never actually heard
  // It is kept as a template and updated anytime an instrument is edited/created
  // We create a deepCopy of it for actually playing.
  public static ComplexOsc currentOscillator;

  public AudioEngine(SauceEngine sauce, final Object mutex) {
    this.sauceEngine = sauce;

    currentOscillator = InstrumentService.getInstrument(defaultInstrument);

    thread = new DacThread(mutex);
    thread.start();
  }

  class DacThread extends Thread {
      Object mutex;
      public DacThread(final Object mutex) {
          this.mutex = mutex;
      }

      private boolean shouldTick = true;
      public void stopTicking() { shouldTick = false; }
      public void startTicking() { shouldTick = true; }

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

            sauceEngine.audioInitialized();
            mutex.notify();
          }

          while (true) {
              if (shouldTick) {
                  dac.tick();
              }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
          dac.close();

          sauceEngine.audioInitialized();
          mutex.notify();

          Log.e(SauceEngine.TAG, "bad time " + ex.toString());
        }
      }
  };

  public boolean isPlaying() {
    return dac.isPlaying();
  }

  public void pauseDac() {
      thread.stopTicking();
  }
  public void playDac() {
      thread.startTicking();
  }

  public boolean isLooping() {
      // Looper.isPlaying() isn't used. Oops! :/
      return looper.recording || looper.defined;
  }

  // Hmm.... should fx things be here?
  public boolean toggleLooperRecording() {
    return looper.toggleRecording();
  }

  public void resetLooper() {
    looper.reset();
  }

  public void undoLooper() {
    looper.undo();
  }

  public ParametricEQ getEq() {
    return this.eq;
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

  public void removeAllOscillators() {
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

    ComplexOsc copy = InstrumentService.copyInstrument(currentOscillator);
    if (copy != null)
      osc = copy;
    else {
      osc = InstrumentService.getInstrument("Sine");
      ActivityService.makeToast("Error: Unable to duplicate instrument");
    }

    connectOsc(osc);
    oscillatorsById.put(id, osc);

    // Ensure new oscillator has up-to-date settings
    updateBaseFreq();

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

  public void updateOscillatorProperty(OscillatorUpdater updater) {
    updater.update(currentOscillator);
    Collection<ComplexOsc> oscs = oscillatorsById.values();

    for (ComplexOsc osc : oscs)
      if (osc != null)
        updater.update(osc);
  }

  public void updateBaseNote(int note) {
    if (note >= 0 && note < Theory.notes.length)
      updateBaseFreq(note, this.octave);
  }

  public void updateBaseOctave(int octave) {
    updateBaseFreq(this.note, octave);
  }

  public void updateBaseFreq(int note, int octave) {
    this.note = note;
    this.octave = octave;

    updateBaseFreq();
  }

  public void updateBaseFreq() {
    float newFreq = Theory.getFrequencyForNote(note + 1, octave);
    Collection<ComplexOsc> oscs = oscillatorsById.values();
    for (ComplexOsc osc : oscs)
      if (osc != null)
        osc.setBaseFreq(newFreq);
  }

  public void setOscillator(ComplexOsc newOsc) {
    currentOscillator = InstrumentService.copyInstrument(newOsc);
    removeAllOscillators();

    connectOsc(newOsc);
    oscillatorsById.put(0, newOsc);

    updateBaseFreq();
  }

  public void setScaleById(String scaleId) {
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

  public boolean toggleRecording() {
    boolean isRecording = dac.toggleRecording();
    if (isRecording) {
      ActivityService.makeToast("Recording.");
    } else {
      File saved = WavWriter.getLastFile();
      if (saved == null) {
        ActivityService.makeToast("Stopped Recording. File could not be saved. I blew it.");
      } else {
        ActivityService.makeToast("Stopped Recording. File saved at: " + saved.getAbsolutePath(), true);

        Intent intent = new Intent(Intent.ACTION_SEND).setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saved));

        // TODO maybe turn this into a Service so we can lose the reference to SauceEngine
        sauceEngine.startActivity(Intent.createChooser(intent, "Share to"));
      }
    }

    return isRecording;
  }
}
