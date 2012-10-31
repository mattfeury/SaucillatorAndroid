package com.mattfeury.saucillator.dev.android.visuals;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.ClickHandler;
import com.mattfeury.saucillator.dev.android.utilities.Fingerable;
import com.mattfeury.saucillator.dev.android.utilities.KnobHandler;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class KnobButton extends SmartRect implements Drawable, Fingerable {
  protected Paint bg, shadow, text, status;
  protected String name;

  private float progress, progressSin, progressCos, lastR, lastTheta = 0f;

  public static final int width = 75,
                          textSize = 14;

  private LinkedList<KnobHandler> handlers = new LinkedList<KnobHandler>();

  public KnobButton(String name) {
    this(name, 0, 0);
  }
  public KnobButton(String name, int x, int y) {
    super(x, y, x + width, y + width);

    changeProgress(0);
    this.name = name;

    bg = new Paint();
    shadow = new Paint();
    status = new Paint();
    text = new Paint();

    bg.setARGB(255, 255,255,255);
    shadow.setARGB(125, 255,255,255);
    status.setARGB(255, 255, 120, 120);
    text.setARGB(255, 255,255,255);
    text.setTextSize(textSize);
    text.setTextAlign(Align.CENTER);

    bg.setStyle(Paint.Style.STROKE);
    bg.setStrokeWidth(2);
    status.setStrokeWidth(5);
  }
  public String getName() {
    return name;
  }
  
  private void changeProgress(float f) {
    if (f < 0) f = 0;
    if (f > 1) f = 1;

    this.progress = f;

    float progressAngle = (Utilities.scale(f / 1f, 45, 315) + 180) % 360;
    double radianAngle = Math.toRadians(progressAngle);
    progressSin = (float) Math.cos(radianAngle);
    progressCos = (float) Math.sin(radianAngle);

    onChange(progress);
  }

  public void draw(Canvas canvas) {
    int radius = width / 2,
        xCenter = (int) (left + radius),
        yCenter = (int) (top + radius);
    canvas.drawCircle(xCenter, yCenter, radius, bg);
    canvas.drawText(name, xCenter, yCenter + textSize / 2, text);

    // Marker
    canvas.drawLine(xCenter + radius * progressCos, yCenter - radius * progressSin, xCenter, yCenter, status);
  }
  public void layoutChanged(int width, int height) {
    //set(left, top, left + width, top + height); 
  }
  public boolean contains(int x, int y) {
    return super.contains(x, y);
  }

  public void addOnChange(KnobHandler handler) {
    handlers.add(handler);
  }
  public void onChange(float progress) {
    for (KnobHandler handler : handlers)
      handler.onChange(progress);
  }
  public void handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int y = (int) event.getY(index);
    final int x = (int) event.getX(index);

    int centerX = (int) (left + width / 2f);
    int centerY = (int) (top + width / 2f);
    int dx = x - centerX;
    int dy = y - centerY;
    float r = (float) Math.sqrt(dx * dx + dy * dy);
    float theta = Utilities.roundFloat((float) Math.atan(dy / (float)dx), 2);

    // Change passed on difference of angles. If angles are the same, compare distance.
    if (theta > lastTheta || (theta == lastTheta && r > lastR && x > centerX) || (theta == lastTheta && r < lastR && x < centerX)) {
      changeProgress(progress + .0075f);
    } else if (theta < lastTheta || (theta == lastTheta && r < lastR && x > centerX) || (theta == lastTheta && r > lastR && x < centerX)) {
      changeProgress(progress - .0075f);
    }

    lastTheta = theta;
    lastR = r;
  }

  @Override
  public boolean shouldClearFloat() {
    return false;
  }
}
