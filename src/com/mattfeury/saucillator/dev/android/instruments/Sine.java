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

  // Can't use Math.sin here due to a bug in the galaxy S4. Fuck that...
  // TODO cache this maybe
  public void fill() {
    final float dt = (float) (2.0 * Math.PI / ENTRIES);
    for (int i = 0; i < ENTRIES; i++) {
      table[i] = (float) (amplitude * FloatMath.sin(i * dt + (float) (oscPhase * Math.PI / 180f)));
    }
  }
}
