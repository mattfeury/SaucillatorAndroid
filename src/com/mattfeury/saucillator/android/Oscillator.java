package com.mattfeury.saucillator.android;

/**
 * Represents an actual oscillator.
 */
public abstract class Oscillator extends UGen {

  public float frequency = 440f;
  public float amplitude = 1.0f;
  
  public float BASE_FREQ = 440f;
  protected int harmonic = 1;

  public void setHarmonic(int h) {
    this.harmonic = h;
  }

  public abstract void setFreq(float freq);
  public abstract void setModRate(int rate);
  public abstract void setModDepth(int depth);
  public abstract void setLag(float rate);

  public abstract void setAmplitude(float amp);
  
  public void setBaseFreq(float freq) {
    BASE_FREQ = freq;
  }

  public synchronized void setFreqByOffset(int[] scale, int offset) {
    float freq = Theory.getFrequencyForScaleNote(scale, BASE_FREQ, offset);
    setFreq(freq);
  }
}
