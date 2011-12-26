package com.mattfeury.saucillator.android;

/**
 * Sawtooth wave oscillator
 */
public class Saw extends BasicOsc {
  
  public Saw() {
    this(1.0f);
  }
  public Saw(int phase) {
    super(phase);
  }
  public Saw(float amp) {
    super(amp);
  }

  public void fill() {
		float dt = (float)(2.0/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) (i * dt - Math.floor(i * dt));
		}
  }
}
