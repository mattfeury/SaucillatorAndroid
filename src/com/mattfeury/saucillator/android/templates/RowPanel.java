package com.mattfeury.saucillator.android.templates;

public class RowPanel extends SmartRect {
  public RowPanel() {
    this(0, 0, 0, 0);
  }
  public RowPanel(int x, int y, int width, int height) {
    super(x, y, x + width, y + height);
  }

  public boolean shouldClearFloat() {
    return true;
  }
}
