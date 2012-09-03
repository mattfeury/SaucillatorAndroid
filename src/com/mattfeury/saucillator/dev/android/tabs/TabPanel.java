package com.mattfeury.saucillator.dev.android.tabs;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.visuals.Drawable;
import com.mattfeury.saucillator.dev.android.visuals.SmartRect;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;

public class TabPanel extends SmartRect {
  protected Paint bg, text;
  protected LinkedList<Drawable> children = new LinkedList<Drawable>();
  private String name;
  private int fontSize = 26;

  public TabPanel(String name) {
    this(name, 0, 0, 0, 0);
  }
  public TabPanel(String name, int x, int y, int width, int height) {
    super(x, y, x + width, y + height);

    this.name = name;

    bg = new Paint();
    bg.setStyle(Paint.Style.STROKE);
    bg.setStrokeWidth(5);
    bg.setARGB(200, 12, 81, 4);

    text = new Paint();
    text.setARGB(255, 255,255,255);
    text.setTextSize(fontSize);
    text.setTextAlign(Align.CENTER);
  }
  
  public void addChild(Drawable child) {
    // TODO make different layouts, not just linear ones
    children.add(child);
  }

  public void draw(Canvas canvas) {
    canvas.drawRect(left, top, right, bottom, bg);
    canvas.drawText(name, (right + left) / 2f, top + fontSize*2, text);

    for (Drawable child : children)
      child.draw(canvas);
  }
}
