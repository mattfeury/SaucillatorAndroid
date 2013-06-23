package com.mattfeury.saucillator.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.android.utilities.*;
import com.mattfeury.saucillator.android.visuals.Drawable;

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
    focusedBg.setARGB(255, 28, 171, 11);
    focusedBg.setTextSize(14);
    focusedBg.setTextAlign(Align.CENTER);
  }

  @Override
  public void draw(Canvas canvas) {
    // TODO reduce repeated calculations here
    if (focused) {
      canvas.drawRect(left + margin + borderSize, top + margin, right - margin - borderSize, top + margin + borderSize, focusedBg); //top line
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top) * .5f + focusedBg.getTextSize() / 2, focusedBg);
    } else {
      canvas.drawRect(left + margin, top + margin, left + margin + borderSize, bottom - margin, bg); //left line
      canvas.drawRect(right - margin - borderSize, top + margin, right - margin, bottom - margin, bg); //right line

      canvas.drawRect(left + margin + borderSize, top + margin, right - margin - borderSize, top + margin + borderSize, bg); //top line
      canvas.drawRect(left + margin + borderSize, bottom - margin - borderSize, right - margin - borderSize, bottom - margin, bg); //bottom line
      
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top) * .5f + text.getTextSize() / 2, text);
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
}
