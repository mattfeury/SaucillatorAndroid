package com.mattfeury.saucillator.dev.android.archive;

import com.mattfeury.saucillator.dev.android.sound.UGen;

/**
 * Exponential envelope
 */
public class ExpEnv extends UGen {
	
	public static float hardFactor = 0.005f;
	public static float medFactor = 0.00005f;
	public static float softFactor = 0.00005f;
	
	boolean state;
	float attenuation;
	float factor = softFactor;
	final float idealMarker = 0.25f;
	float marker = idealMarker;
	
	public synchronized void setActive(boolean nextState) {
		state = nextState;
	}
	
	public synchronized void setFactor(float nextFactor) {
		factor = nextFactor;
	}
	
	public synchronized void setGain(float gain) {
		marker = gain * idealMarker;
	}
	 
	public synchronized boolean render(final float[] buffer) {
		if(!state && attenuation < 0.0001f) return false;
		if(!renderKids(buffer)) return false;
		
		for(int i = 0; i < CHUNK_SIZE; i++) {
			buffer[i] *= attenuation;
			if(!state) {
				attenuation += (0-attenuation)*factor;
			} else {
				attenuation += (marker-attenuation)*factor;
			}
		}
		return true;
	}
}
