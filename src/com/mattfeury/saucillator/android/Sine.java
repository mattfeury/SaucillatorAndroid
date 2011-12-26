package com.mattfeury.saucillator.android;

import android.util.FloatMath;

/**
 * Sine wave oscillator
 */
public class Sine extends BasicOsc {
  
  public Sine() {
    super();
  }
  public Sine(int phase) {
    super(phase);
  }
  public Sine(float amp) {
    super(amp);
  }

  public void fill() {
    final float dt = (float)(2.0*Math.PI/ENTRIES);
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = amplitude * FloatMath.sin(i*dt + (float)(oscPhase * Math.PI / 180f));
    }
  }
}
