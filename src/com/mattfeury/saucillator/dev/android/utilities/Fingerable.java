package com.mattfeury.saucillator.dev.android.utilities;

import android.view.MotionEvent;

public interface Fingerable {
  public void handleTouch(int id, MotionEvent event);
}
