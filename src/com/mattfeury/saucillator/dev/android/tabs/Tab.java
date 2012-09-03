package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.visuals.RectButton;
import com.mattfeury.saucillator.dev.android.visuals.TabSelector;

import android.graphics.Canvas;

public class Tab {

  private java.util.Random gen = new java.util.Random();
  private int id = gen.nextInt(100000);
  private TabSelector selector = new TabSelector(""+id);
  private RectButton tab = new RectButton("HI I'M A TAB BITCH : " + id);
  
  public boolean isInSelector(int x, int y) {
    return selector.contains(x, y);
  }

  public void drawSelector(Canvas canvas, boolean isCurrent) {
    if (isCurrent)
      selector.focus();
    else
      selector.unfocus();

    selector.draw(canvas);
  }

  public void drawTab(Canvas canvas) {
    tab.draw(canvas);
  }

  public void setSelector(int x, int y, int selectorWidth, int selectorHeight) {
    selector.set(x, y, selectorWidth, selectorHeight);
  }

  public void setTab(int x, int y, int tabWidth, int tabHeight) {
    tab.set(x, y, tabWidth, tabHeight);
  }
}
