package com.mattfeury.saucillator.android.utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

  public static float roundFloat(float f, int numDecimalPoints) {
    for(int i=0; i < numDecimalPoints; i++) f *= 10f;
    int rounded = Math.round(f);
    float truncated = rounded;
    for(int i=0; i < numDecimalPoints; i++) truncated /= 10f;

    return truncated;
  }

  /**
   * Returns a deep copy of the object, or null if the object cannot
   * be serialized.
   * 
   * Courtesy: http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
   */
  public static Object deepCopy(Object orig) {
    Object obj = null;
    try {
      // Write the object out to a byte array
      FastByteArrayOutputStream fbos =
              new FastByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(fbos);
      out.writeObject(orig);
      out.flush();
      out.close();

      // Retrieve an input stream from the byte array and read
      // a copy of the object back in.
      ObjectInputStream in =
          new ObjectInputStream(fbos.getInputStream());
      obj = in.readObject();
    } catch(Exception e) {
      e.printStackTrace();
    }
    return obj;
  }


}
