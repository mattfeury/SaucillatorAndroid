package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.Canvas;

public interface Drawable {
  public void draw(Canvas canvas);
  public void layoutChanged(int width, int height);
}
