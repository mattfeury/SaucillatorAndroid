package com.mattfeury.saucillator.android;

import android.util.FloatMath;

public class LfoOsc extends WtOsc {
	public static final int BITS = 16;
	public static final int ENTRIES = 1<<(BITS-1); //bit depth: 2^(bits-1)
	public static final int MASK = ENTRIES-1;
	
	private float phase;
	private float cyclesPerSample;

  private boolean lfoEnabled = true;
  public int modDepth = 0;
  public int modRate = 0; //in Hz
<<<<<<< Updated upstream
  private float rate = 0.2f; //rate at which the LFO lags between frequency changes
=======
  private float rate = 1.0f; //rate at which the LFO lags between frequency changes
>>>>>>> Stashed changes
  private float t = 0f;
  private float frequency = 440f;
  private float lagOut;

  final float[] table;

  public LfoOsc() {
    table = new float[ENTRIES];
  }

  public synchronized void setFrequency(float freq) {
    cyclesPerSample = freq/SAMPLE_RATE;
  }
  public synchronized void setFreq(float freq) {
    frequency = freq;
  }

  public void setModRate(int rate) {
    modRate = rate;
  }
  public void setModDepth(int depth) {
    modDepth = depth;
  }

  public synchronized boolean render(final float[] buffer) { // assume t is in 0.0 to 1.0
		if(! isPlaying) {
			return true;
		}

    if (lfoEnabled)
      modulate();

		for(int i = 0; i < CHUNK_SIZE; i++) {
      float scaled = phase*ENTRIES;
      final float fraction = scaled-(int)scaled;
      final int index = (int)scaled;
      buffer[i] += (1.0f-fraction)*table[index&MASK]+fraction*table[(index+1)&MASK];
      phase = (phase+cyclesPerSample) - (int)phase; 
    }

    return true;
	}

  public synchronized void modulate() {
  	float lfo = updateLfo();
  	float lag = updateLag();
  	setFrequency(lfo + lag);
  }
  public synchronized float updateLfo() {
    if (modRate == 0) return 0f;

    // TODO why does .05 work so well here?
    // also, can we do this smoother?
    float lfoFn = modDepth/2 * FloatMath.sin(modRate * t);
    t = (float) ((t + .05f) % (2f*Math.PI*modRate));
    return lfoFn;
  }
  public synchronized float updateLag() {
    lagOut = lagOut + rate * (frequency - lagOut);
    return lagOut;
  }

  public LfoOsc fillWithSin() {
    final float dt = (float)(2.0*Math.PI/ENTRIES);
    for(int i = 0; i < ENTRIES; i++) {
      table[i] = FloatMath.sin(i*dt);
    }
    return this;
  }

  public LfoOsc fillWithHardSin(final float exp) {
    final float dt = (float)(2.0*Math.PI/ENTRIES);
    for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) Math.pow(FloatMath.sin(i*dt),exp);
		}
		return this;
	}
	
	public LfoOsc fillWithZero() {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = 0;
		}
		return this;
	}
	
	public LfoOsc fillWithSqr() {
		return fillWithSqrWithAmp(1.0f);
	}
	
	public LfoOsc fillWithSqrWithAmp(float amp) {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = i<ENTRIES/2?amp:-1f*amp;
		}
		return this;
	}
	
	public LfoOsc fillWithSqrDuty(float fraction) {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float)i/ENTRIES<fraction?1f:-1f;
		}
		return this;
	}
	
	public LfoOsc fillWithSaw() {
		float dt = (float)(2.0/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) (i * dt - Math.floor(i * dt));
		}
		return this;
	}
}
