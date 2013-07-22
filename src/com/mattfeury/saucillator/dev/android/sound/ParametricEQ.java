package com.mattfeury.saucillator.dev.android.sound;

/**
 * Written with the help of this wonderful resource:
 * http://epubl.luth.se/1402-1773/2003/044/LTU-CUPP-03044-SE.pdf
 * 
 * Q is unit-less, as usual, but it seems to correlate that:
 * 0.33 = about 1,500 Hz affected
 * 12 = about 8,000 Hz affected
 */
public class ParametricEQ extends UGen {

  public static final float minFreq = 20,
                            maxFreq = (SAMPLE_RATE - 50f) / 2f, //if we go much higher, we get strAnGe fx (e.g. ringing)
                            minQ = .33f,
                            maxQ = 12f,
                            minGain = -12,
                            maxGain = 12;

  private float frequency = (maxFreq + minFreq) / 2f,
                gain = (maxGain + minGain) / 2f,
                q = (maxQ + minQ) / 2f;

  // Internal variables used for calculating IIF
  // Sorry these are named so C-style (i.e. shittily),
  // but they are derived from an equation (a0 = "a sub 0")
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
    this.frequency = freq;
    recalculate();
  }
  public void setQ(float q) {
    this.q = q;
    recalculate();
  }
  public void setGain(float gain) {
    this.gain = gain;
    recalculate();
  }
  public float getFrequency() {
    return frequency;
  }
  public float getQ() {
    return q;
  }
  public float getGain() {
    return gain;
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
