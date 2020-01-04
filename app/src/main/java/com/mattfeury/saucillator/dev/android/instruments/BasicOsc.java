package com.mattfeury.saucillator.dev.android.instruments;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;

import android.util.FloatMath;

/**
 * I am a basic oscillator.
 * I am represented by a simple mathematical wave form (Sine, Square, etc)
 */
public abstract class BasicOsc extends Oscillator {
	public static final int BITS = 16;
	public static final int ENTRIES = 1<<(BITS-1); //bit depth: 2^(bits-1)
	public static final int MASK = ENTRIES-1;
	
	private float phase;
	private float cyclesPerSample;

  private boolean lfoEnabled = true;
  public int modDepth = 0;
  public int modRate = 0; //in Hz

  private float rate = AudioEngine.DEFAULT_LAG; //rate at which the LFO lags between frequency changes
  private float t = 0f;
  private float lagOut;
  private float preLfoFrequency = frequency * harmonic; //since LFO modifies the actual frequency variable

  final float[] table;

  public BasicOsc() {
    this(1.0f);
  }
  public BasicOsc(int phase) {
    this(1.0f, phase);
  }
  public BasicOsc(float amp) {
    this(amp, 0);
  }
  public BasicOsc(float amp, int phase) {
    name = "BasicInstrument";

    this.oscPhase = phase;
    amplitude = amp;
    table = new float[ENTRIES];
    fill();   
  }

  public synchronized void updateFrequency(float freq) {
    frequency = freq;
    cyclesPerSample = frequency/SAMPLE_RATE;
  }
  public synchronized void setFreq(float freq) {
    // Don't change to the same frequency we're already on since the LFO may be on
    if (preLfoFrequency == freq * harmonic)
      return;

    preLfoFrequency = freq * this.harmonic;
    t = 0;

    updateFrequency(freq * this.harmonic);
  }
  public void resetFreq() {
    updateFrequency(preLfoFrequency);
  }

  public void setModRate(int rate) {
    resetFreq();
    modRate = rate;
  }
  public void setModDepth(int depth) {
    resetFreq();
    modDepth = depth;
  }
  public int getModRate() {
    return modRate;
  }
  public int getModDepth() {
    return modDepth;
  }
  public void setLag(float rate) {
    this.rate = 1.0f - rate;
  }
  public float getLag() {
    return 1.0f - rate;
  }

  public void rendered() {
    //Noop
  }
  
  public synchronized boolean render(final float[] buffer) { // assume t is in 0.0 to 1.0
		if(! isPlaying()) {
			return false;
		}

    if (lfoEnabled)
      modulate();

		for(int i = 0; i < CHUNK_SIZE; i++) {
      float scaled = phase*ENTRIES;
      final float fraction = scaled-(int)scaled;
      final int index = (int)scaled;
      buffer[i] += amplitude*((1.0f-fraction)*table[index&MASK]+fraction*table[(index+1)&MASK]);
      phase = (phase+cyclesPerSample) - (int)phase; 
    }

    rendered();

    return true;
	}

  public synchronized void modulate() {
    float lfo = updateLfo();
    float lag = updateLag();
    updateFrequency((lfo + lag) * harmonic);
  }
  public synchronized float updateLfo() {
    if (modRate == 0) return 0f;

    // TODO why does .05 work so well here?
    // also, can we do this smoother?
    float lfoFn = modDepth/2 * (float)Math.sin(modRate * t);
    t = (float) ((t + .05f) % (2f*Math.PI*modRate));
    return lfoFn;
  }
  public synchronized float updateLag() {
    lagOut = lagOut + rate * (preLfoFrequency / harmonic - lagOut);
    return lagOut;
  }

	public BasicOsc fillWithZero() {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = 0;
		}
		return this;
	}  
  //TODO make these instruments or get rid of them
  public BasicOsc fillWithHardSin(final float exp) {
    final float dt = (float)(2.0*Math.PI/ENTRIES);
    for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) Math.pow(Math.sin(i*dt),exp);
		}
		return this;
	}
	
	
	public BasicOsc fillWithSqrDuty(float fraction) {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float)i/ENTRIES<fraction?1f:-1f;
		}
		return this;
	}

}
