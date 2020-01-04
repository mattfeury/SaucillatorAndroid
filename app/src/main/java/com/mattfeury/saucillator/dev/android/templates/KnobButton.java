package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.*;
import android.util.FloatMath;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class KnobButton extends Button implements IntervalButton {
  protected Paint status;

  private float progress, progressSin, progressCos, lastR, lastTheta = 0f;

  public static final int textSize = 14;
  private static final float margin = 0.2f;

  public KnobButton(String name) {
    this(name, 0);
  }
  public KnobButton(String name, float progress) {
    this(name, progress, 0, 0);
  }
  public KnobButton(String name, float progress, int x, int y) {
    super(name, x, y, x, y); // no width/height by default?

    changeProgress(progress);

    status = new Paint();
    status.setColor(SauceView.PAD_COLOR);
    status.setStrokeWidth(5);
    
    bg.setARGB(255, 255,255,255);
    bg.setStyle(Paint.Style.STROKE);
    bg.setStrokeWidth(2);
  }

  public void changeProgress(float f) {
    if (f < 0) f = 0;
    if (f > 1) f = 1;

    this.progress = f;

    float progressAngle = (Utilities.scale(f / 1f, 45, 315) + 180) % 360;
    float radianAngle = (float)Math.toRadians(progressAngle);
    progressSin = (float) Math.cos(radianAngle);
    progressCos = (float) Math.sin(radianAngle);

    handle(progress);
  }

  private int getRadius() {
    return (int)(Math.min(right - left, bottom - top) / 2 * (1f - margin));
  }

  @Override
  public void draw(Canvas canvas) {
    int radius = getRadius(),
        rectCenterX = (int) ((right - left) / 2f + left),
        rectCenterY = (int) ((bottom - top) / 2f + top),
        xCenter = rectCenterX,
        yCenter = rectCenterY;

    canvas.drawCircle(xCenter, yCenter, radius, bg);
    canvas.drawText(name, xCenter, yCenter + textSize / 2, text);
    canvas.drawLine(xCenter + radius * progressCos, yCenter - radius * progressSin, xCenter, yCenter, status);
  }
  
  @Override
  public void layoutChanged(int width, int height) {
    //FIXME what is this i don't even
    // See if we need to recalculate knob dimensions, otherwise just remove this
  }

  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    final int index = event.findPointerIndex(id);
    final int y = (int) event.getY(index);
    final int x = (int) event.getX(index);

    int radius = getRadius();
    int centerX = (int) (left + radius);
    int centerY = (int) (top + radius);
    boolean hasHistory = event.getHistorySize() > 0;

    final float lastX = hasHistory ? event.getHistoricalX(index, 0) : centerX;
    final float lastY = hasHistory ? event.getHistoricalY(index, 0) : centerY;

    final float dx = x - lastX;
    final float dy = lastY - y;
    float r = (float) Math.sqrt(dx * dx + dy * dy);
    float theta = (float) Math.toDegrees(Math.atan2(dy, dx));
    if (theta < 0) {
      theta += 360;
    }

    if (theta > 315 || theta < 135) {
      changeProgress(progress + .015f);
    } else {
      changeProgress(progress - .015f);
    }

    // Change passed on difference of angles. If angles are the same, compare distance.
    /*if (theta > lastTheta || (theta == lastTheta && r > lastR && x > centerX) || (theta == lastTheta && r < lastR && x < centerX)) {
      // FIXME this magic .015f. it should probably not increment progress, but set it directly to the angle of the finger to the center 
      changeProgress(progress + .015f);
    } else if (theta < lastTheta || (theta == lastTheta && r < lastR && x > centerX) || (theta == lastTheta && r > lastR && x < centerX)) {
      changeProgress(progress - .015f);
    }*/

    lastTheta = theta;
    lastR = r;

    return new Full<Fingerable>(this);
  }
}
