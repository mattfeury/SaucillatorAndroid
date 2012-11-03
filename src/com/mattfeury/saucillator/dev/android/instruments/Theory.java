package com.mattfeury.saucillator.dev.android.instruments;

/**
 * Helper stuff for music theory.
 */
public class Theory {

  // String keys must match a value in the string resources array
  public enum Scale {
    CHROMATIC {
      public String toString() {
        return "Chromatic";
      }
    },
    MAJOR {
      public String toString() {
        return "Major";
      }
    },
    MINOR {
      public String toString() {
        return "Minor";
      }
    },
    MINOR_BLUES {
      public String toString() {
        return "Blues";
      }
    },
    PENTATONIC {
      public String toString() {
        return "Pentatonic";
      }
    }
  }

  // First one is default
  public static final Scale[] scales = {Scale.PENTATONIC, Scale.MAJOR, Scale.MINOR, Scale.CHROMATIC, Scale.MINOR_BLUES};
  public static final String[] notes = {"A","A#/Bb","B","C","C#/Db","D","D#/Eb","E","F","F#/Gb","G","G#/Ab"};
  public static final Integer[] octaves = {1, 2, 3, 4, 5, 6};
  public static final Integer OCTAVE = 4; // Default.

  //music info
  public static int[] chromaticScale = {0,1,2,3,4,5,6,7,8,9,10,11};
  public static int[] majorScale = {0,2,4,5,7,9,11};
  public static int[] minorScale = {0,2,3,5,7,8,10};
  public static int[] minorBluesScale = {0,3,5,6,7,10,12};
  public static int[] pentatonicScale = {0,3,5,7,10,12};

  public static float A1 = 55f;

  // A = 1, A#/Bb = 2, B = 3, etc...
  // A1 = (1,1)
  public static float getFrequencyForNote(int note, int octave) {
    float noteFreq = (float)(A1 * Math.pow(2, (note - 1) / 12f));
    return noteFreq * (float)Math.pow(2, octave - 1);
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
  public static double getScaleIntervalFromOffset(int[] scale, int offset) {
    return (scale[Math.abs(offset) % scale.length] + 12*((int)((offset)/scale.length)));
  }

}
