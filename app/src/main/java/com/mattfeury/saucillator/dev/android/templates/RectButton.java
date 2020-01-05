package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class RectButton extends Button {
  protected Paint focusedBg;

  public RectButton(String name) {
    this(name, 0, 0, 0, 0);
  }

  public RectButton(String name, int x, int y, int width, int height) {
    super(name, x, y, x + width, y + height);

    focusedBg = new Paint();
    focusedBg.setColor(SauceView.ALERT_COLOR);
    focusedBg.setTextSize(14);
    focusedBg.setTextAlign(Align.CENTER);
  }

  @Override
  public void draw(Canvas canvas) {
    this.draw(canvas, focused ? focusedBg : bg);
  }

  public void draw(Canvas canvas, Paint paint) {
    canvas.drawRect(left + margin, top + margin, right - margin, bottom - margin, paint);

    Align align = text.getTextAlign();
    if (align == Align.CENTER) {
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top) * .5f + text.getTextSize() / 2, text);
    } else if (align == Align.LEFT) {
      canvas.drawText(name, left + padding, top + (bottom - top) * .5f + text.getTextSize() / 2, text);
    }
  }

  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    if (Utilities.idIsDown(id, event))
      handle(null);

    // We don't want this to keep tracking subsequent touch events, so we handle it and return Empty
    return new Empty<Fingerable>();
  }

  public void setFocus(boolean focus) {
    focused = focus;
  }
  public void focus() {
    focused = true;
  }
  public void unfocus() {
    focused = false;
  }
  public boolean toggleFocus() {
    focused = ! focused;
    return focused;
  }

  public void setClear(boolean clear) {
    this.clearFloat = clear;
  }
  @Override
  public boolean shouldClearFloat() {
    return clearFloat;
  }
  public void setTextPaint(Paint textPaint) {
    this.text = textPaint;
  }

  @Override
  public void setTextSize(int size) {
    super.setTextSize(size);

    focusedBg.setTextSize(size);
  }

  public void setBackgroundColor(int color) {
    bg.setColor(color);
  }
}
