package com.mattfeury.saucillator.dev.android.sound;

public class Limiter {

  public static void limit(final float[] buffer) {
    // Determine max absolute value. This could get nasty performance-wise
    float max = 0, min = 0, peak = 0;
    for (float f : buffer) {
      if (f > max)
        max = f;
      if (f < min)
        min = f;
    }
    peak = Math.max(Math.abs(min), max);

    if (peak > 1) {
      for (int i = 0; i < buffer.length; i++)
        buffer[i] *= 1f / peak;
    }
  }

}
