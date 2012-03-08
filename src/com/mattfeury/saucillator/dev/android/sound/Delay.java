package com.mattfeury.saucillator.dev.android.sound;

/**
 * Creates a delay effect by repeating samples
 */
public class Delay extends UGen {
	final float[] line;
	int pointer;
	int length;
  private float decay = 0.5f;
	boolean enabled = true;
	
	public Delay(int length) {
		super();
		this.length = length;
		line = new float[UGen.SAMPLE_RATE];
	}

  public float getDecay() {
    return decay;
  }
  public void setDecay(float decay) {
    this.decay = decay;
  }
  public int getRate() {
    return length;
  }
  public void setRate(int length) {
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
		boolean didWork = renderKids(buffer);

    if (length == 0 || ! enabled)
      return didWork;
		
		final float[] localLine = line;
		for(int i = 0; i < CHUNK_SIZE; i++) {
      buffer[i] = buffer[i] - decay*localLine[pointer];
      localLine[pointer] = buffer[i];
      pointer = (pointer+1)%length;
		}
		
		return didWork; //this doesn't actually mean anything here
	}
}
