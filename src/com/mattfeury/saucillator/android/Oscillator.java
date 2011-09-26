package com.mattfeury.saucillator.android;

public abstract class Oscillator extends UGen {

  public float frequency = 440f;
  public float amplitude = 1.0f;
  
  public float BASE_FREQ = 440f;

  public abstract void setFreq(float freq);
  public abstract void setModRate(int rate);
  public abstract void setModDepth(int depth);

  public synchronized void setFreqByOffset(int[] scale, int offset) {
    float freq = Instrument.getFrequencyForScaleNote(scale, BASE_FREQ, offset);
    setFreq(freq);
  }
}
