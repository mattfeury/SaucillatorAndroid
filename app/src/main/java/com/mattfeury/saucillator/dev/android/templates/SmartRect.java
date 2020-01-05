package com.mattfeury.saucillator.dev.android.templates;

import com.mattfeury.saucillator.dev.android.visuals.Drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Just a RectF that lets us define it by width and height rather than right and bottom measurements.
 * We also mixin some of our Drawable functionality here since layout elements generally extend this.
 */
public abstract class SmartRect extends RectF implements Drawable {

  protected Paint defaultPaint = new Paint();
  protected int colspan = 1, rowspan = 1;

  public SmartRect(int x, int y, int width, int height) {
    super(x, y, x + width, y + height);

    defaultPaint.setARGB(0, 0, 0, 0);
  }

  @Override
  public void set(int x, int y, int width, int height) {
    super.set(x, y, x + width, y + height);

    calculateTextSize();
  }

  public int calculateTextSize() {
    int newSize = ((int) Math.min(((bottom - top) / 6), (right - left) / 4));

    this.setTextSize(newSize);

    return newSize;
  }

  public void setTextSize(int size) {
    // Noop. Override this.
  }

  public void draw(Canvas canvas) {
    canvas.drawRect(this, defaultPaint);
  }
  public void layoutChanged(int width, int height) {
    // This isn't right but it's not used right now. Width and height are the values for the entire view
    set(left, top, left + width, top + height); 
  }
  public boolean contains(int x, int y) {
    return super.contains(x, y);
  }

  public boolean shouldClearFloat() {
    return false;
  }

  public int getColspan() {
    return colspan;
  }
  public int getRowspan() {
    return rowspan;
  }
  public void setColspan(int span) {
    this.colspan = span;
  }
  public void setRowspan(int span) {
    this.rowspan = span;
  }
}
