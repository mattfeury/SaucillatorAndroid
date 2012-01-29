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
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) (i * dt - Math.floor(i * dt));
		}
  }
}
