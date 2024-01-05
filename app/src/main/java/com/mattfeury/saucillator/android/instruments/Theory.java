package com.mattfeury.saucillator.android.instruments;

/**
 * Helper stuff for music theory.
 */
public class Theory {

  // String keys must match a value in the string resources array
  public enum Scale {
    PENTATONIC_MINOR(0, 3, 5, 7, 10),
    PENTATONIC_MAJOR(0, 2, 4, 7, 9),
    MAJOR /* AKA IONIAN */(0, 2, 4, 5, 7, 9, 11),
    MINOR /* AKA AEOLIAN */(0, 2, 3, 5, 7, 8, 10),
    CHROMATIC(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
    MINOR_BLUES(0, 3, 5, 6, 7, 10),
    PENTATONIC_SUSPENDED(0, 2, 5, 7, 10),
    PENTATONIC_MAN_GONG(0, 3, 5, 8, 10),
    PENTATONIC_RITUSEN(0, 2, 5, 7, 9),
    HARMONIC_MINOR(0, 2, 3, 5, 7, 8, 11),
    ASCENDING_MELODIC_MINOR(0, 2, 3, 5, 7, 9, 11),
    DORIAN(0, 2, 3, 5, 7, 9, 10),
    PHRYGIAN(0, 1, 3, 5, 7, 8, 10),
    LYDIAN(0, 2, 4, 6, 7, 9, 11),
    ;

    public final int[] scaleOffsets;
    public final String displayName;

    Scale(int... scaleOffsets) {
        this.scaleOffsets = scaleOffsets;

        SET_DISPLAY_NAME:
        {
            String[] words = name().split("_");
            StringBuilder namebuilder = new StringBuilder();
            for (String word : words)
                namebuilder.append(word.charAt(0)).append(word.substring(1).toLowerCase()).append(" ");
            displayName = namebuilder.toString().trim();
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
  }

  public static final String[] notes = "C,C#,D,Eb,E,F,F#,G,Ab,A,Bb,B".split(",");
  public static final Integer[] octaves = {1, 2, 3, 4, 5, 6};
  public static final Integer OCTAVE = 4; // Default.

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
