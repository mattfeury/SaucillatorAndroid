package com.mattfeury.saucillator.android;

import android.util.FloatMath;

public class Square extends BasicOsc {
  
  protected float amp = 1.0f;
  public Square() {
    this(1.0f);
  }
  public Square(float amp) {
    super(amp);
  }

  public void fill() {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = i<ENTRIES/2?amplitude:-1f*amplitude;
		}
  }
}
