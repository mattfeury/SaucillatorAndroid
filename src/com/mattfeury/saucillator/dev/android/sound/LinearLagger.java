package com.mattfeury.saucillator.dev.android.sound;

import java.io.Serializable;

import com.mattfeury.saucillator.dev.android.utilities.Utilities;

/**
 * Output approaches Input linearly over a time 'rate'.
 */
public class LinearLagger implements Serializable {
  protected float in, out, rate, tInSamples;
  
  public LinearLagger(float current, float approaches, float t) {
    this.in = approaches;
    this.out = current;
    this.tInSamples = t * UGen.SAMPLE_RATE;
    setRate(t);
  }

  public float update() {
    int sign = (in < out) ? -1 : 1;

    if ((sign == 1 && out >= in) || (sign == -1 && out <= in))
      out = in;
    else 
      out += in / tInSamples * rate * sign;

    out = Utilities.roundFloat(out, 5);

    return out;
  }
  public void setRate(float t) {
    // Calculate how many CHUNK_SIZES we need to fill 'rate' seconds
    this.rate = (float)UGen.SAMPLE_RATE / UGen.CHUNK_SIZE * t;
  }
  public void setInput(float in) {
    this.in = in;
  }
  // synonymous function. seems clearer
  public void approach(float in) {
    this.setInput(in);
  }

  public float getOutput() {
    return out;
  }

}
