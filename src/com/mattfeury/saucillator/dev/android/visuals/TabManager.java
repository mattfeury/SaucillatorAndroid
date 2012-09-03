package com.mattfeury.saucillator.dev.android.visuals;

import java.util.LinkedList;
import android.graphics.Canvas;

public class TabManager implements Drawable {
  private LinkedList<Tab> tabs = new LinkedList<Tab>();
  private Tab currentTab = null;

  public static final float selectorWidthNoTabs = 1f;
  public static final float selectorWidthWithTabs = 0.25f;

  public static float selectorWidth = selectorWidthWithTabs;

  public TabManager() {
    // Add some tabs
    addTab(new Tab());
    addTab(new Tab());
    addTab(new Tab());
  }

  private void addTab(Tab tab) {
    tabs.add(tab);

    if (tabs.size() == 1)
      setCurrentTab(tab);
  }

  private void setCurrentTab(Tab tab) {
    this.currentTab = tab;
    
    selectorWidth = selectorWidthWithTabs;
    SauceView.tabOpen();
  }
  public void hideCurrentTab() {
    this.currentTab = null;
    
    selectorWidth = selectorWidthNoTabs;
    SauceView.tabClosed();
}
  public void toggleCurrentTabAt(int x, int y) {
    for (Tab tab : tabs) {
      if (tab.isInSelector(x, y)) {
        if (isCurrent(tab)) {
          hideCurrentTab();
        } else {
          setCurrentTab(tab);
        }

        return;
      }
    }
  }
  public boolean isCurrent(Tab tab) {
    return tab.equals(currentTab);
  }

  public void draw(Canvas canvas) {
    for (Tab tab : tabs) {
      boolean isCurrent = currentTab != null && tab.equals(currentTab);
      tab.drawSelector(canvas, isCurrent);
    }

    if (currentTab != null)
      currentTab.drawTab(canvas);
  }

  public void layoutChanged(int width, int height) { 
    int i = 0;
    int tabCount = tabs.size();
    int selectorHeight = (int) (height / (float)tabCount);
    int selectorWidth = (int) (width * SauceView.controllerWidth * TabManager.selectorWidth);
    int tabWidth = (int) (width * SauceView.controllerWidth * (1f - TabManager.selectorWidth));

    for (final Tab tab : tabs) {
      tab.setSelector(0, selectorHeight * i, selectorWidth, selectorHeight);
      tab.setTab((int) (width * SauceView.controllerWidth * TabManager.selectorWidth), 0, tabWidth, height);
      i++;
    }
  }
}
