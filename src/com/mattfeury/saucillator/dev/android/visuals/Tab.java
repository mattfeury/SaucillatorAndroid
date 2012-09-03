package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.Canvas;

public class Tab {

  private RectButton selector = new RectButton("Testy");
  private RectButton tab = new RectButton("HI I'M A TAB BITCH");

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
