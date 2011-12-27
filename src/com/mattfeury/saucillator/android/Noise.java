package com.mattfeury.saucillator.android;

import android.util.FloatMath;

/**
 * Noise oscillator (filled randomly)
 */
public class Noise extends BasicOsc {
  
  public Noise() {
    super();
  }
  public Noise(int phase) {
    super(phase);
  }
  public Noise(float amp) {
    super(amp);
  }

  public void fill() {
    java.util.Random gen = new java.util.Random();
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = amplitude * (gen.nextFloat() * 2.0f - 1);
    }
  }
}
