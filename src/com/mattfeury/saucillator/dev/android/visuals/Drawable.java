package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.Canvas;

public interface Drawable {
  public void draw(Canvas canvas);
  public void layoutChanged(int width, int height);

  public void set(int x, int y, int width, int height);
  public boolean contains(int x, int y);
  
  public boolean shouldClearFloat();
  public int getColspan();
  public int getRowspan();
}
