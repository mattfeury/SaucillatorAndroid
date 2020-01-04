package com.mattfeury.saucillator.dev.android.instruments;

import com.mattfeury.saucillator.dev.android.sound.Lagger;
import com.mattfeury.saucillator.dev.android.sound.UGen;

/**
 * Represents an actual oscillator.
 */
public abstract class Oscillator extends UGen {

  protected String name = "Unknown";

  protected float frequency = 440f;
  protected float amplitude = 1.0f;
  protected int oscPhase = 0;

  protected float BASE_FREQ = 440f;
  protected int harmonic = 1;

  public void setHarmonic(int h) {
    this.harmonic = h;
  }
  public int getHarmonic() {
    return harmonic;
  }

  // Callback called post rendering
  public abstract void rendered();
  
  public abstract void setFreq(float freq);
  public abstract void setModRate(int rate);
  public abstract void setModDepth(int depth);
  public abstract void setLag(float rate);
  public abstract float getLag();

  public abstract int getModRate();
  public abstract int getModDepth();

  public float getAmplitude() {
    return amplitude;
  }
  public void setAmplitude(float amp) {
    this.amplitude = amp;
  }
  public void factorAmplitude(float factor) {
    this.amplitude *= factor;
  }

  public void setBaseFreq(float freq) {
    BASE_FREQ = freq;
  }

  public synchronized void setFreqByOffset(int[] scale, int offset) {
    float freq = Theory.getFrequencyForScaleNote(scale, BASE_FREQ, offset);
    setFreq(freq);
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int getPhase() {
    return oscPhase;
  }
  public void setPhase(int phase) {
    this.oscPhase = phase;
    fill();
  }
  // This [re]fills our table for BasicOsc.
  // For ComplexOscs, it refills the children.
  public abstract void fill();  
}
