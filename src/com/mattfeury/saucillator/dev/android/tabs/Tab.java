package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.utilities.Fingerable;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;
import com.mattfeury.saucillator.dev.android.visuals.RectButton;

import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class Tab {

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
  public TabPanel getPanel() {
    return panel;
  }

  public Box<Fingerable> handlePanelTouch(int id, MotionEvent event) {
    return panel.handleTouch(id, event);
  }
}
