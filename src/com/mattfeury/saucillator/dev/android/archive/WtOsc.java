package com.mattfeury.saucillator.dev.android.archive;

import com.mattfeury.saucillator.dev.android.sound.UGen;

import android.util.FloatMath;

public class WtOsc extends UGen {
	public static final int BITS = 16;
	public static final int ENTRIES = 1<<(BITS-1); //bit depth: 2^(bits-1)
	public static final int MASK = ENTRIES-1;
	
	private float phase;
	private float cyclesPerSample;
	
	final float[] table;
	
	public WtOsc () {
		table = new float[ENTRIES];
	}
	
	public synchronized void setFreq(float freq) {
		cyclesPerSample = freq/SAMPLE_RATE;
	} 
	
	public synchronized boolean render(final float[] buffer) { // assume t is in 0.0 to 1.0
		
		if(! isPlaying()) {
			return true;
		}
		
		for(int i = 0; i < CHUNK_SIZE; i++) {
			float scaled = phase*ENTRIES;
			final float fraction = scaled-(int)scaled;
			final int index = (int)scaled;
			buffer[i] += (1.0f-fraction)*table[index&MASK]+fraction*table[(index+1)&MASK];
			phase = (phase+cyclesPerSample) - (int)phase; 
		}

		
		return true;
	}
	
	public WtOsc fillWithSin() {
		final float dt = (float)(2.0*Math.PI/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) Math.sin(i*dt);
		}
		return this;
	}
	
	public WtOsc fillWithHardSin(final float exp) {
		final float dt = (float)(2.0*Math.PI/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) Math.pow(Math.sin(i*dt),exp);
		}
		return this;
	}
	
	public WtOsc fillWithZero() {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = 0;
		}
		return this;
	}
	
	public WtOsc fillWithSqr() {
		return fillWithSqrWithAmp(1.0f);
	}
	
	public WtOsc fillWithSqrWithAmp(float amp) {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = i<ENTRIES/2?amp:-1f*amp;
		}
		return this;
	}
	
	public WtOsc fillWithSqrDuty(float fraction) {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float)i/ENTRIES<fraction?1f:-1f;
		}
		return this;
	}
	
	public WtOsc fillWithSaw() {
		float dt = (float)(2.0/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) (i * dt - Math.floor(i * dt));
		}
		return this;
	}
}