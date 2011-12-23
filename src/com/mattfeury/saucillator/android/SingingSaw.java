package com.mattfeury.saucillator.android;

/**
 * A complex instrument that is supposed to sound like a Singing Saw
 * Right now it is just playing chords rather than a single note.
 * Ideally it should have its own settings for Lag, LFO, etc that are inherit
 * to its timbre.
 */
public class SingingSaw extends ComplexOsc {
  private BasicOsc fundamental, third, fifth; //sine
  public SingingSaw() {
    super();

    fundamental = new Sine(0.5f);
    third = new Sine(0.5f);
    fifth = new Sine(0.5f);

    fill(fundamental, third, fifth);
  }

  public void setFreq(float freq) {
    frequency = freq;
    fundamental.setFreq(freq);
    third.setFreq(Instrument.getFrequencyForScaleNote(Instrument.majorScale, freq, 2));
    fifth.setFreq(Instrument.getFrequencyForScaleNote(Instrument.majorScale, freq, 4));
  }
}
