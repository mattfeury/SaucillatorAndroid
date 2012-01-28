package com.mattfeury.saucillator.android.sound;

/**
 * Creates a delay effect by repeating samples
 */
public class Delay extends UGen {
	final float[] line;
	int pointer;
	int length;
	boolean enabled = true;
	
	public Delay(int length) {
		super();
		this.length = length;
		line = new float[UGen.SAMPLE_RATE];
	}

  public void updateRate(int length) {
		this.length = length;
  }
  public void enable() {
  	enabled = true;
  }
  public void disable() {
  	enabled = false;
  }
  public boolean isEnabled() {
  	return enabled;
  }
	
	public boolean render(final float[] buffer) {
		renderKids(buffer);
		
		final float[] localLine = line;
		for(int i = 0; i < CHUNK_SIZE; i++) {
      buffer[i] = buffer[i] - 0.5f*localLine[pointer];
      localLine[pointer] = buffer[i];
      pointer = (pointer+1)%length;
		}
		
		return true; //this doesn't actually mean anything here
	}
}
