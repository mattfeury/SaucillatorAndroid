package com.mattfeury.saucillator.android;

public abstract class Oscillator extends UGen {
  public float frequency = 440f;

  public abstract void setFreq(float freq);
}
