package com.mattfeury.saucillator.android;

public class Utilities {

  /**
   * Scale a percentage (0.0 to 1.0) to fit in given bounds (lower - higher)
   */
  public static float scale(float percentage, float lower, float higher) {
    return percentage * (higher - lower) + lower;
  }
  public static float scale(float percentage, float higher) {
    return scale(percentage, 0f, higher);
  }
  public static int scale(float percentage, int lower, int higher) {
    return (int)scale(percentage, (float)lower, (float)higher);
  }
  public static int scale(float percentage, int higher) {
    return (int)scale(percentage, (float)higher);
  }

  /**
   * Convert a scaled number to its percentage between given bounds
   */
  public static float unscale(float scaled, float lower, float higher) {
    return (scaled - lower) / (float)(higher - lower);
  }
  public static float unscale(float scaled, float higher) {
    return unscale(scaled, 0f, higher);
  }
  public static int unscale(float scaled, int lower, int higher) {
    return (int)unscale(scaled, (float)lower, (float)higher);
  }
  public static int unscale(float scaled, int higher) {
    return (int)unscale(scaled, (float)higher);
  }

}
