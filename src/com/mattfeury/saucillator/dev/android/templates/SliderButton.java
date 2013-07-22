package com.mattfeury.saucillator.dev.android.templates;

import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.SauceView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

public class SliderButton extends Button implements IntervalButton {
  protected Paint status;

  protected float progress = 0f;
  protected int min = 0, max = 1, current = 0;

  public SliderButton(String name) {
    this(0, 1, name);
  }
  public SliderButton(int min, int max, String name) {
    super(name, 0, 0, 0, 0);

    this.min = min;
    this.max = max;
    changeProgress(0);

    status = new Paint();
    status.setColor(SauceView.PAD_COLOR);
    status.setTextAlign(Paint.Align.CENTER);
    setBorder(borderSize);

    this.bg.setColor(Color.WHITE);
  }

  @Override
  public int calculateTextSize() {
    int newSize = super.calculateTextSize();
    status.setTextSize(newSize);

    return newSize;
  }

  @Override
  public void setBorder(int size) {
    super.setBorder(size);

    status.setStrokeWidth(borderSize);
    bg.setStrokeWidth(Math.max(borderSize - 2, 0));
  }
  
  public Button setBounds(int min, int max, int current) {
    this.min = min;
    this.max = max;
    changeProgress((current - min) / ((float)max - min));
    
    return this;
  }

  public void changeProgress(float p) {
    if (p < 0) p = 0;
    if (p > 1) p = 1;

    this.progress = p;
    this.current = Utilities.scale(progress, min, max);

    handle(current);
  }

  @Override
  public void draw(Canvas canvas) {
    int height = (int) (bottom - top);
    int centerY = (int) (top + height / 2f);
    int progressWidth = (int) ((right - left - margin * 2) * progress);
    int width = (int) (right - left);

    // Name
    canvas.drawText("" + getName(), left + width / 2, top + height * .25f, bg);

    // Horizontal Line
    canvas.drawLine(margin + left, centerY, right - margin, centerY, bg);

    // Marker
    //canvas.drawLine(margin + left + progressWidth, top + height / 3f, margin + left + progressWidth, bottom - height / 3f, status);
    canvas.drawCircle(margin + left + progressWidth, top + height / 2, 15, status);
    canvas.drawText("" + current, margin + left + progressWidth, bottom - margin, status);
  }

  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int x = (int) event.getX(index);

    float width = right - left - margin * 2;
    float progress = Math.min(width, (x - left - margin * 2)) / (width);
    changeProgress(progress);

    return new Full<Fingerable>(this);
  }
}
