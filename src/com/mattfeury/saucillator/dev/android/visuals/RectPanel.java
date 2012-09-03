package com.mattfeury.saucillator.dev.android.visuals;

import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;

public class RectPanel extends SmartRect {
  private Paint bg;
  private LinkedList<Drawable> children = new LinkedList<Drawable>();

  public RectPanel() {
    this(0, 0, 0, 0);
  }
  public RectPanel(int x, int y, int width, int height) {
    super(x, y, x + width, y + height);

    bg = new Paint();
    bg.setARGB(200, 12, 81, 4);
  }

  public void draw(Canvas canvas) {
    canvas.drawRect(left, top, right, bottom, bg);

    for (Drawable child : children)
      child.draw(canvas);
  }
}
