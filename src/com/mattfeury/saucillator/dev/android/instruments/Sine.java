package com.mattfeury.saucillator.dev.android.instruments;


import android.util.FloatMath;

/**
 * Sine wave oscillator
 */
public class Sine extends BasicOsc {
  public Sine() {
    super();
    name = "Sine";
  }
  public Sine(int phase) {
    super(phase);
    name = "Sine";
  }
  public Sine(float amp) {
    super(amp);
    name = "Sine";
  }

  public void fill() {
    final float dt = (float)(2.0*Math.PI/ENTRIES);
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = amplitude * FloatMath.sin(i*dt + (float)(oscPhase * Math.PI / 180f));
    }
  }
}
