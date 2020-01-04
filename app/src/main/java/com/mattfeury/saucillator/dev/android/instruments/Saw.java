package com.mattfeury.saucillator.dev.android.instruments;


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
    float dt = (amplitude * 2f / ENTRIES);
    int phaseOffset = (int)((float)(oscPhase / (float)360) * ENTRIES);

    // Cannot gaurantee this phase shift is working,
    // because the sawtooth is not a mirror image of itself
    // reflected across a y axis. Therefore, 0 + 180 do not  
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = (i + phaseOffset) % ENTRIES * dt - amplitude;
    }
  }
}
