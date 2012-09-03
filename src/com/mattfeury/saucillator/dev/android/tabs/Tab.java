package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.visuals.RectButton;

import android.graphics.Canvas;

public abstract class Tab {

  // Remove me;
  private java.util.Random gen = new java.util.Random();
  private int id = gen.nextInt(1000) * gen.nextInt(10000);

  protected String name;
  protected TabSelector selector;
  protected TabPanel panel;

  private AudioEngine engine;
  
  public Tab(String name, AudioEngine engine) {
    this.engine = engine;
    
    this.name = name;
    selector = new TabSelector(name);
    panel = new TabPanel(name);
  }

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

  public void drawPanel(Canvas canvas) {
    panel.draw(canvas);
  }

  public void setSelector(int x, int y, int selectorWidth, int selectorHeight) {
    selector.set(x, y, selectorWidth, selectorHeight);
  }

  public void setPanel(int x, int y, int tabWidth, int tabHeight) {
    panel.set(x, y, tabWidth, tabHeight);
  }

  public TabSelector getSelector() {
    return selector;
  }
}
