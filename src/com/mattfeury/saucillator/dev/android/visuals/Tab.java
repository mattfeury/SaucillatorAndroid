package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.Canvas;

public class Tab {

  private java.util.Random gen = new java.util.Random();
  private int id = gen.nextInt(100000);
  private RectButton selector = new RectButton(""+id);
  private RectButton tab = new RectButton("HI I'M A TAB BITCH : " + id);
  
  public boolean isInSelector(int x, int y) {
    return selector.contains(x, y);
  }

  public void drawSelector(Canvas canvas, int x, int y, int selectorWidth, int selectorHeight, boolean isCurrent) {
    if (isCurrent)
      selector.focus();
    else
      selector.unfocus();

    selector.set(x, y, selectorWidth, selectorHeight);
    selector.draw(canvas);
  }

  public void drawTab(Canvas canvas, int x, int y, int tabWidth, int tabHeight) {
    tab.set(x, y, tabWidth, tabHeight);
    tab.draw(canvas);
  }
}
