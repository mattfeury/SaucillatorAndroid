package com.mattfeury.saucillator.dev.android.templates;

import com.mattfeury.saucillator.dev.android.utilities.*;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class SliderButton extends Button {
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
    status.setARGB(200, 246, 255, 66);
    status.setTextAlign(Paint.Align.CENTER);
    setBorder(borderSize);
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

  private void changeProgress(float p) {
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

    // Ends
    canvas.drawLine(margin + left, margin + top, margin + left, bottom - margin, bg);
    canvas.drawLine(right - margin, margin + top, right - margin, bottom - margin, bg);
    
    // Status
    canvas.drawLine(margin + left, centerY, right - margin, centerY, bg);
    canvas.drawLine(margin + left, centerY, margin + left + progressWidth - borderSize / 2f, centerY, status); 

    // Marker
    canvas.drawLine(margin + left + progressWidth, top + height / 3f, margin + left + progressWidth, bottom - height / 3f, status);
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
