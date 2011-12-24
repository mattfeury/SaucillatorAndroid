package com.mattfeury.saucillator.android;

public class Limiter {

  public static void limit(final float[] buffer) {
    // Determine max value. This could get nasty performance-wise
    float max = 0;
    for (float f : buffer) {
      if (f > max)
        max = f;
    }

    if (max > 1) {
      for (int i = 0; i < buffer.length; i++)
        buffer[i] *= 1.0f / max;
    }
  }
  
}
