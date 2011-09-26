package com.mattfeury.saucillator.android;

import android.util.FloatMath;

public class Saw extends BasicOsc {
  
  protected float amp = 1.0f;
  public Saw() {
    this(1.0f);
  }
  public Saw(float amp) {
    super(amp);
  }

  public void fill() {
		float dt = (float)(2.0/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) (i * dt - Math.floor(i * dt));
		}
  }
}
