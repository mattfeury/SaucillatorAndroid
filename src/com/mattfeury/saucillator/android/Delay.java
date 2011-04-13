package com.mattfeury.saucillator.android;

public class Delay extends UGen {
	final float[] line;
	int pointer;
	
	public Delay(int length) {
		super();
		line = new float[length];
	}
	
	public boolean render(final float[] buffer) {
		renderKids(buffer);
		
		final float[] localLine = line;
		final int lineLength = line.length;
		for(int i = 0; i < CHUNK_SIZE; i++) {
			buffer[i] = buffer[i] - 0.5f*localLine[pointer];
			localLine[pointer] = buffer[i];
			pointer = (pointer+1)%lineLength;
		}
		
		// ugh, looks like we can never be sure it's silent
		// without checking every sample because of the feedback
		return true; 
	}
}
