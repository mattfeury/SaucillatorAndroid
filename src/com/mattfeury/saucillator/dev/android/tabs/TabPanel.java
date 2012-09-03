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

  private static final float contentPadding = .15f;

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

    recalculateChildren();
  }

  // recalculate children locations
  public void recalculateChildren() {
    int i = 0;
    int width = (int) (right - left);
    int height = (int) (bottom - top);

    int childCount = children.size();
    int contentPadding = (int) (width * TabPanel.contentPadding);
    int contentWidth = (int) (width - contentPadding * 2);
    int contentHeight = (int) (height - fontSize*4) / childCount;

    for (Drawable child : children) {
      child.set((int) (left + contentPadding), (int) (top + contentPadding), contentWidth, contentHeight);
      i++;
    }
  }

  public void draw(Canvas canvas) {
    canvas.drawRect(left, top, right, bottom, bg);
    canvas.drawText(name, (right + left) / 2f, top + fontSize*2, text);

    for (Drawable child : children)
      child.draw(canvas);
  }

  @Override
  public void set(int x, int y, int width, int height) {
    super.set(x, y, width, height);
    
    recalculateChildren();
  }
}
