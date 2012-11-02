package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.Box;
import com.mattfeury.saucillator.dev.android.utilities.ClickHandler;
import com.mattfeury.saucillator.dev.android.utilities.Empty;
import com.mattfeury.saucillator.dev.android.utilities.Fingerable;
import com.mattfeury.saucillator.dev.android.utilities.Full;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.visuals.Drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class RectButton extends SmartRect implements Drawable, Fingerable {
  protected Paint bg, text, focusedBg;
  private int borderWidth = 5;
  protected String name;
  protected boolean focused = false;
  public static final int textWidth = -8; //in pixels, i guess? just an estimate.

  private LinkedList<ClickHandler> handlers = new LinkedList<ClickHandler>();
  protected boolean clearFloat;

  public RectButton(String name) {
    this(name, 0, 0, 0, 0);
  }
  public RectButton(String name, int x, int y, int width, int height) {
    super(x, y, x + width, y + height);
    
    this.name = name;

    bg = new Paint();
    focusedBg = new Paint();
    text = new Paint();

    bg.setARGB(200, 12, 81, 4);
    focusedBg.setARGB(255, 28, 171, 11);
    text.setARGB(255, 255,255,255);
    focusedBg.setTextSize(14);
    focusedBg.setTextAlign(Align.CENTER);
    text.setTextSize(14);
    text.setTextAlign(Align.CENTER);
    
    bg.setStrokeWidth(5);

  }
  public String getName() {
    return name;
  }

  @Override
  public void draw(Canvas canvas) {
    if (focused) {
      canvas.drawRect(left, top, right, top + borderWidth, focusedBg); //top line
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top)* .5f, focusedBg);
    } else {
      canvas.drawRect(left + borderWidth, top, right - borderWidth, top + borderWidth, bg); //top line
      canvas.drawRect(left, top, left + borderWidth, bottom - borderWidth, bg); //left line
      canvas.drawRect(left, bottom - borderWidth, right, bottom, bg); //bottom line
      canvas.drawRect(right - borderWidth, top, right, bottom - borderWidth, bg); //right line
      
      canvas.drawText(name, (right + left) / 2f, top + (bottom - top)* .5f, text);
    }
  }

  public void addOnClick(ClickHandler handler) {
    handlers.add(handler);
  }
  public void click() {
    for (ClickHandler handler : handlers)
      handler.onClick();
  }
  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int action = event.getAction();
    final int actionCode = action & MotionEvent.ACTION_MASK;
    final int actionIndex = event.getActionIndex();
    final int actionId = event.getPointerId(actionIndex);

    // TODO add vibrator here
    if (Utilities.idIsDown(id, event))
      click();

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
}
