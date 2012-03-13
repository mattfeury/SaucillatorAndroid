package com.mattfeury.saucillator.dev.android.sound;

import java.io.Serializable;

import com.mattfeury.saucillator.dev.android.utilities.Utilities;

/**
 * Output approaches Input exponentially by rate.
 * When rate is 0, output = input. When rate is 1, output is constant. Rate should rarely be 1.
 *
 * Output = Output + Rate * (Input - Output)
 */
public class Lagger implements Serializable {
  protected float in, out, rate = .025f;
  
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
    out = Utilities.roundFloat(out, 5);
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
