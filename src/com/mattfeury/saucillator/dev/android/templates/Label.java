package com.mattfeury.saucillator.dev.android.templates;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class Label extends SmartRect {

  protected String text = "";
  protected Paint paint;
  protected boolean clearFloat = false;

  protected int textSize = 14;
  protected int minTextSize = 1;

  public Label(String text) {
    this(text, 0, 0, 0, 0);
  }
  public Label(String text, int x, int y, int width, int height) {
    super(x, y, x + width, y + height);

    this.text = text;

    paint = new Paint();
    paint.setARGB(255, 255, 255, 255);
    paint.setTextSize(textSize);
    paint.setTextAlign(Align.CENTER);
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.drawText(text, (right + left) / 2f, top + (bottom - top) * .5f + textSize / 2, paint);
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setClear(boolean clear) {
    this.clearFloat = clear;
  }
  @Override
  public boolean shouldClearFloat() {
    return clearFloat;
  }
  public void setTextSize(int size) {

    if (size < this.minTextSize) {
      this.setTextSize(minTextSize);
    } else {
      this.textSize = size;
      paint.setTextSize(size);
    }
  }
  public void setMinTextSize(int min) {
    this.minTextSize  = min;
  }
}
