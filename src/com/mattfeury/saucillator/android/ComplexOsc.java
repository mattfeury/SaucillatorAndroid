package com.mattfeury.saucillator.android;

import java.util.LinkedList;

/**
 * A complex oscillator.
 * An oscillator that is made up of BasicOscs and sums them together.
 */
public class ComplexOsc extends Oscillator {

  protected LinkedList<Oscillator> components;

  public ComplexOsc() {
    this(1.0f);
  }
  public ComplexOsc(float amp) {
    amplitude = amp;
    components = new LinkedList<Oscillator>();
  }

  protected void fill(Oscillator... oscs) {
    for(Oscillator osc : oscs) {
      osc.isPlaying = true; //we manage playback here, so all the children should be available for rendering
      components.add(osc);
      osc.chuck(this);
    }
  }

  public void togglePlayback() {
    isPlaying = !isPlaying;
  }

  public void setFreq(float freq) {
    for(Oscillator osc : components)
      osc.setFreq(freq * this.harmonic);
  }

  public void setModRate(int rate) {
    for(Oscillator osc : components)
      osc.setModRate(rate);
  }
  public void setModDepth(int depth) {
    for(Oscillator osc : components)
      osc.setModDepth(depth);
  }
  public void setLag(float rate) {
    for(Oscillator osc : components)
      osc.setLag(rate);
  }
  public void setAmplitude(float amp) {
    for(Oscillator osc : components)
      osc.setAmplitude(amp);
  }


  public synchronized boolean render(final float[] buffer) { // assume t is in 0.0 to 1.0
		if(! isPlaying) {
			return true;
		}
    return renderKids(buffer);
	}
}
