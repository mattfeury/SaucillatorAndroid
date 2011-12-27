package com.mattfeury.saucillator.android;

public class ParametricEQ extends UGen {

  private float frequency = 440, //20hz - half the sample rate
                gain = -10f, // -12 to 12db
                q = 6f; // 0.33 - 12

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
  public void setFrequency(float freq) {
    freq = freq * (SAMPLE_RATE / 2f - 20f) + 20;
    this.frequency = (freq >= SAMPLE_RATE / 2f) ? (SAMPLE_RATE-1) / 2f : freq;
    recalculate();
  }
  public void setQ(float q) {
    this.q = q * 12f;
    if (q < .33) q = .33f;
    recalculate();
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

      //android.util.Log.i("EQ", "orig: " + xn + " / new: " + yn);

      buffer[i] = yn;
    }

    return true;
    }
  }

}
