package com.mattfeury.saucillator.android;


public class Instrument {

  //music info
  public static int[] chromaticScale = {0,1,2,3,4,5,6,7,8,9,10,11};
  public static int[] majorScale = {0,2,4,5,7,9,11};
  public static int[] minorScale = {0,2,3,5,7,8,10};
  public static int[] minorBluesScale = {0,3,5,6,7,10,12};
  public static int[] pentatonic = {0,3,5,7,10,12};

  public Instrument() {
  }
  
  public static float getFrequencyForScaleNote(int[] scale, float baseFreq, int offset) {
    double scaleOffset = getScaleIntervalFromOffset(scale, offset);
    float freq = (float)(Math.pow(2,((scaleOffset) / 12.0)) * baseFreq);
    return freq;
  }
  
  /*
   * fairly important method here. Given an integer offset calculate the actual offset
   * from a scale. 
   *
   * ie, given an offset of 1, we will find the offset for the first note in the scale
   */
  public static double getScaleIntervalFromOffset(int[] scale, int offset)
  {
    return (scale[offset % scale.length] + 12*((int)((offset)/scale.length)));
  }

}
