package com.mattfeury.saucillator.dev.android.tabs;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;
import com.mattfeury.saucillator.dev.android.visuals.LayoutDefinitions;

import android.graphics.Canvas;
import android.view.MotionEvent;

public class TabManager implements Drawable {
  private LinkedList<Tab> tabs = new LinkedList<Tab>();
  private Tab currentTab = null;
  private int width = 0, height = 0, colspan = 1, rowspan = 1;

  public void addTab(final Tab tab) {
    tabs.add(tab);

    tab.getSelector().addHandler(new Handler<Boolean>() {
      public void handle(Boolean o) {
        if (isCurrent(tab)) {
          hideCurrentTab();
        } else {
          setCurrentTab(tab);
        }
      }
    });

    if (tabs.size() == 1)
      setCurrentTab(tab);
  }

  private void setCurrentTab(Tab tab) {
    this.currentTab = tab;

    LayoutDefinitions.tabOpen();
  }
  public void hideCurrentTab() {
    this.currentTab = null;
    
    LayoutDefinitions.tabClosed();
  }
  /*public void toggleCurrentTabAt(int x, int y) {
    for (Tab tab : tabs) {
      if (tab.isInSelector(x, y)) {
        tab.getSelector().click();
        return;
      }
    }
  }*/
  public boolean isCurrent(Tab tab) {
    return tab.equals(currentTab);
  }

  public void draw(Canvas canvas) {
    for (Tab tab : tabs) {
      boolean isCurrent = currentTab != null && tab.equals(currentTab);
      tab.drawSelector(canvas, isCurrent);
    }

    if (currentTab != null)
      currentTab.drawPanel(canvas);
  }

  public void layoutChanged(int width, int height) { 
    set(0, 0, width, height);

    for (Tab tab : tabs) {
      tab.layoutChanged(width, height);
    }
  }
  public void set(int x, int y, int width, int height) {
    this.height = height;

    int i = 0;
    int tabCount = tabs.size();
    int selectorHeight = (int) (height / (float)tabCount);
    int selectorWidth = (int) (width * LayoutDefinitions.controllerWidth * LayoutDefinitions.tabSelectorWidth);
    int tabWidth = (int) (width * LayoutDefinitions.controllerWidth * (1f - LayoutDefinitions.tabSelectorWidth));

    this.width = selectorWidth + tabWidth;

    for (Tab tab : tabs) {
      tab.setSelector(0, selectorHeight * i, selectorWidth, selectorHeight);
      tab.setPanel((int) (width * LayoutDefinitions.controllerWidth * LayoutDefinitions.tabSelectorWidth), 0, tabWidth, height);
      i++;
    }
  }
  public boolean contains(int x, int y) {
    return x < this.width && y < this.height; 
  }

  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int y = (int) event.getY(index);
    final int x = (int) event.getX(index);

    if (currentTab != null && currentTab.getPanel().contains(x, y)) {
      return currentTab.handlePanelTouch(id, event);
    } else {
      for (Tab tab : tabs) {
        if (tab.isInSelector(x, y) && Utilities.idIsDown(id, event)) {
          TabSelector selector = tab.getSelector();
          selector.handle(null);
          // TODO this could return Empty since we don't want any more events hitting it for MOVE events
          return new Full<Fingerable>(selector);
        }
      }
      return new Empty<Fingerable>();
    }
  }

  @Override
  public boolean shouldClearFloat() {
    return true;
  }
  public int getColspan() {
    return colspan;
  }
  public int getRowspan() {
    return rowspan;
  }
}
