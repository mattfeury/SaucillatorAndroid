package com.mattfeury.saucillator.dev.android.templates;


public class ColumnPanel extends SmartRect {
  public ColumnPanel() {
    this(0, 0, 0, 0);
  }
  public ColumnPanel(int x, int y, int width, int height) {
    super(x, y, x + width, y + height);
  }

  public boolean shouldClearFloat() {
    return false;
  }
}
