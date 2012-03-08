package com.mattfeury.saucillator.dev.android.sound;

public class Vibrato {

  public static void apply(final float[] buffer) {
    int length = buffer.length;

    // note: length/2 because my length is in bytes but sample is 16 bit
    for(int i=0; i<(length/2); i++ ) {
      float rawDataShiftPos = (float) (i + Math.sin(i/44100.0)*100.0); // 44100.0=vibrato rate  100.0=depth
      int dataShiftPos = (int)rawDataShiftPos;
      float dataShiftPercent = rawDataShiftPos - dataShiftPos;
      if( (dataShiftPos>=0) && (dataShiftPos<(length/2)) ) {
        // accounts for offsets that fall between sample points -- distribute them between the two points
        buffer[dataShiftPos]+=(float)buffer[i]*(1.0-dataShiftPercent);
        buffer[dataShiftPos+1]+=(float)buffer[i]*dataShiftPercent;
      }
    }
  }

}
