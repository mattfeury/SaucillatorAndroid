package com.mattfeury.saucillator.dev.android.tabs;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.ClickHandler;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;
import com.mattfeury.saucillator.dev.android.visuals.LayoutDefinitions;

import android.graphics.Canvas;

public class TabManager implements Drawable {
  private LinkedList<Tab> tabs = new LinkedList<Tab>();
  private Tab currentTab = null;

  public void addTab(final Tab tab) {
    tabs.add(tab);

    tab.getSelector().addOnClick(new ClickHandler() {
      public void onClick() {
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
  public void toggleCurrentTabAt(int x, int y) {
    for (Tab tab : tabs) {
      if (tab.isInSelector(x, y)) {
        tab.getSelector().click();
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
    int selectorWidth = (int) (width * LayoutDefinitions.controllerWidth * LayoutDefinitions.tabSelectorWidth);
    int tabWidth = (int) (width * LayoutDefinitions.controllerWidth * (1f - LayoutDefinitions.tabSelectorWidth));

    for (Tab tab : tabs) {
      tab.setSelector(0, selectorHeight * i, selectorWidth, selectorHeight);
      tab.setTab((int) (width * LayoutDefinitions.controllerWidth * LayoutDefinitions.tabSelectorWidth), 0, tabWidth, height);
      i++;
    }
  }
}
