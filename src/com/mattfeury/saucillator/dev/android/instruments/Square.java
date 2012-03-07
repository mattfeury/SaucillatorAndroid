package com.mattfeury.saucillator.dev.android.instruments;


/**
 * Square wave oscillator
 */
public class Square extends BasicOsc {
  public Square() {
    this(1.0f);
    name = "Square";
  }
  public Square(int phase) {
    super(phase);
    name = "Square";
  }
  public Square(float amp) {
    super(amp);
    name = "Square";
  }

  public void fill() {
    int phaseOffset = (int)((float)(oscPhase / (float)360) * ENTRIES);
    int phaseCursor = phaseOffset;
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = ((phaseCursor % ENTRIES) < ENTRIES / 2) ? amplitude : -1f * amplitude;
      phaseCursor++;
    }
  }
}
