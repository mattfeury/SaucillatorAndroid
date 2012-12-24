package com.mattfeury.saucillator.dev.android.instruments;

/**
 * FIXME this is Pulse now
 */
public class Pulse extends BasicOsc {
  public Pulse() {
    this(1.0f);
    name = "Pulse";
  }
  public Pulse(int phase) {
    super(phase);
    name = "Pulse";
  }
  public Pulse(float amp) {
    super(amp);
    name = "Pulse";
  }

  public void fill() {
    int phaseOffset = (int)((float)(oscPhase / (float)360) * ENTRIES);
    int phaseCursor = phaseOffset;
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = ((phaseCursor % ENTRIES) < ENTRIES * 0.25f) ? amplitude : -1f * amplitude;
      phaseCursor++;
    }
  }
}
