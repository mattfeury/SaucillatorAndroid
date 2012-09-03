package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.RectF;

/**
 * Talk about unnecessary. Just a RectF that lets us define it by width and height
 */
public abstract class SmartRect extends RectF {

  public SmartRect(int x, int y, int width, int height) {
    super(x, y, x + width, y + height);
  }

  public void set(int x, int y, int width, int height) {
    super.set(x, y, x + width, y + height);       
  }  
}
