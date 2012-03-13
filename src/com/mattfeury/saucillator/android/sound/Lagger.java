package com.mattfeury.saucillator.android.sound;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Output approaches Input exponentially by rate.
 * When rate is 0, output = input. When rate is 1, output is constant. Rate should rarely be 1.
 *
 * Output = Output + Rate * (Input - Output)
 */
public class Lagger implements Serializable {
  protected float in, out, rate = .025f;

  // When approaching 0, the lagger will never reach it as it will keep constantly dividing.
  // To fix this, we round to 5 decimal places so we can achieve a "zero" inevitably.
  protected static final DecimalFormat df = new DecimalFormat("#.00000");
  
  public Lagger(float f, float approaches, float rate) {
    this.in = approaches;
    this.out = f;
    setRate(rate);
  }

  // TODO figure out a way to do this linearly
  public float update() {
    // Internally, in = out when rate is 1. This doesn't make sense logically though,
    // so we expose the inverse to functionality.
    out += (1f - rate) * (in - out);
    out = Float.valueOf(df.format(out));
    return out;
  }
  public void setRate(float rate) {
    this.rate = rate*rate;
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
