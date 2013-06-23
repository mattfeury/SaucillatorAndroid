package com.mattfeury.saucillator.android.utilities;

import android.view.MotionEvent;

public interface Fingerable {
  public Box<Fingerable> handleTouch(int id, MotionEvent event);
}
