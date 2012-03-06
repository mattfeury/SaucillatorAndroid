package com.mattfeury.saucillator.android.instruments;


/**
 * Sawtooth wave oscillator
 */
public class Saw extends BasicOsc {
  public Saw() {
    this(1.0f);
    name = "Saw";
  }
  public Saw(int phase) {
    super(phase);
    name = "Saw";
  }
  public Saw(float amp) {
    super(amp);
    name = "Saw";
  }

  public void fill() {
    float dt = (float)(2.0/ENTRIES);
    int phaseOffset = (int)((float)(oscPhase / (float)360) * ENTRIES);

    // Can't be certain that this phase stuff works because it doesn't
    // destructively cancel at 180 degrees.
    for(int i = phaseOffset; i < ENTRIES + phaseOffset; i++) {
      table[i - phaseOffset] = (float) (i * dt - Math.floor(i * dt));
    }
  }
}
