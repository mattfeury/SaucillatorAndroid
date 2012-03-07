package com.mattfeury.saucillator.dev.android.instruments;


/**
 * Noise oscillator (filled randomly)
 */
public class Noise extends BasicOsc {
  public Noise() {
    super();
    name = "Noise";
  }
  public Noise(int phase) {
    super(phase);
    name = "Noise";
  }
  public Noise(float amp) {
    super(amp);
    name = "Noise";
  }

  public void fill() {
    java.util.Random gen = new java.util.Random();
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = amplitude * (gen.nextFloat() * 2.0f - 1);
    }
  }
}
