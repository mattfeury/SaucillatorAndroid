package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.utilities.Fingerable;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;

import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class Tab {

  protected String name, id;
  protected TabSelector selector;
  protected TabPanel panel;

  protected AudioEngine engine;

  public Tab(String id, AudioEngine engine) {
    this(id, id, engine);
  }

  public Tab(String id, String name, AudioEngine engine) {
    this.engine = engine;

    this.id = id;
    this.name = name;
    selector = new TabSelector(id);
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

  public void layoutChanged(int width, int height) {
    panel.layoutChanged(width, height);
  }

  public Box<Fingerable> handlePanelTouch(int id, MotionEvent event) {
    return panel.handleTouch(id, event);
  }
}
