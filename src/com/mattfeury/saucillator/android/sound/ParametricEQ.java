package com.mattfeury.saucillator.android.sound;

import com.mattfeury.saucillator.android.utilities.Utilities;

public class ParametricEQ extends UGen {

  private float frequency = 440, //20hz - half the sample rate
                gain = -10f, // -12 to 12db
                q = 6f; // 0.33 - 12

  private final float minFreq = 20,
                      maxFreq = (SAMPLE_RATE - 2f) / 2f,
                      minQ = .33f,
                      maxQ = 12f;

  private float a0, a1, a2, b0, b1, b2;
  private float xm1 = 0, xm2 = 0, ym1 = 0, ym2 = 0;

  // TODO abstract me to an effect interface or something
  private boolean enabled = true;

  public ParametricEQ() {
    recalculate();
  }
  public ParametricEQ(float frequency, float gain, float q) {
    this.frequency = frequency;
    this.gain = gain;
    this.q = q;

    recalculate();
  }

  /**
   * These setters expect a percentage value (0.0 to 1.0) that they then scale to actual bounds
   */
  public void setFrequency(float freq) {
    this.frequency = Utilities.scale(freq, minFreq, maxFreq);
    recalculate();
  }
  public void setQ(float q) {
    this.q = Utilities.scale(q, minQ, maxQ);
    recalculate();
  }
  public float getFrequency() {
    return Utilities.unscale(frequency, minFreq, maxFreq);
  }
  public float getQ() {
    return Utilities.unscale(q, minQ, maxQ);
  }

  private void recalculate() {
    synchronized(this) {
      float A, omega, cs, sn, alpha;

      A = (float) Math.pow(10f, gain / 40.0f);
      omega = (float) ((2f * Math.PI * frequency) / SAMPLE_RATE);
      sn = (float) Math.sin(omega);
      cs = (float) Math.cos(omega);
      alpha = (float) (sn / (2.0f * q));

      b0 = 1f + alpha * A;
      b1 = -2f * cs;
      b2 = 1f - alpha * A;
      a0 = 1f + alpha / A;
      a1 = -2f * cs;
      a2 = 1f - alpha / A;
    }
  }

  public boolean render(final float[] buffer) {
    synchronized(this) {
    boolean didWork = renderKids(buffer);

    if (! enabled)
      return didWork;

    int length = buffer.length;

    float xn, yn;
    for(int i = 0; i < length; i++) {
      xn = buffer[i];
      yn = (b0 * xn + b1 * xm1 + b2 * xm2 - a1 * ym1 - a2 * ym2) / a0;

      xm2 = xm1;
      xm1 = xn;
      ym2 = ym1;
      ym1 = yn;

      buffer[i] = yn;
    }

    return true;
    }
  }

}
