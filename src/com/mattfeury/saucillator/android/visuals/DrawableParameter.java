package com.mattfeury.saucillator.android.visuals;

import java.text.DecimalFormat;

import com.mattfeury.saucillator.android.utilities.Utilities;

import android.graphics.*;

/**
 * A parameter that is drawn on the screen and can be dragged to be changed.
 */
public class DrawableParameter {

  protected float x = 0f, y = 0f;
  protected int lastX = 0, lastY = 0, maxX = 1, maxY = 1;
  protected Paint paint = new Paint(),
                  textPaint = new Paint();
  protected int radius = 30,
                textSize = 14;

  protected String fullName = "Param",
                   smallName = "#",
                   xParam = "?",
                   yParam = "?";

  protected boolean enabled = true;
  protected boolean showCoords = true;

  private static final DecimalFormat df = new DecimalFormat("#.00");
  private int textOffset = 40;
 
  // The radius of the circle that encompasses this drawable circle
  // that is the touchable area for this parameters
  public int touchRadius = radius * 2;

  protected ParameterHandler handler;

  public DrawableParameter(String fullName, String smallName, String xParam, String yParam, ParameterHandler handler, float x, float y) {
    this(fullName, smallName, xParam, yParam, handler, x, y, 1, 1);
  }
  public DrawableParameter(String fullName, String smallName, String xParam, String yParam,
                            ParameterHandler handler, float x, float y, int maxX, int maxY) {
    this.handler = handler;
    this.fullName = fullName;
    this.smallName = smallName;
    this.xParam = xParam;
    this.yParam = yParam;

    this.x = Utilities.scale(x, SauceView.controllerWidth, 1);
    this.y = Utilities.scale(y, 1f - SauceView.padHeight, 1);
    this.maxX = maxX;
    this.maxY = maxY;
    init();
  }
  protected void init() {
    paint.setARGB(150, 28, 201, 11);//(150, 200, 0, 0);
    textPaint.setARGB(150, 200, 200, 200);
    textPaint.setTextSize(textSize);
  }

  public String getFullName() {
    return fullName;
  }
  
  private float formatFloat(float f) {
    return Float.valueOf(df.format(f));
  }

  public void draw(Canvas canvas) {
    if (enabled) {
      lastX = (int) (this.x * canvas.getWidth());
      lastY = canvas.getHeight() - (int) (this.y * canvas.getHeight());
      canvas.drawCircle(lastX, lastY, radius, paint);
      canvas.drawText(smallName, lastX - (radius / (1f+smallName.length())), lastY + (radius / 4f), textPaint);

      if (showCoords) {
        float xVal = formatFloat(Utilities.unscale(x, SauceView.controllerWidth, 1) * maxX);
        float yVal = formatFloat(Utilities.unscale(y, 1f - SauceView.padHeight, 1) * maxY);
        String text = "(" + xParam + ": " + xVal + ", " + yParam + ": " + yVal + ")";
        
        int textWidth = text.length() * 5, //random number. width of a character in pixels?
            textHeight = textSize,
            textX = (lastX - textWidth - textOffset) > 0 ?
                      lastX - textWidth - textOffset : lastX + textOffset,
            textY = (lastY - textHeight - textOffset) > 0 ?
                      lastY - textHeight - textOffset : lastY + textOffset;
        canvas.drawText(text, textX, textY, textPaint);
      }
    }
  }
  public void toggle() {
    enabled = ! enabled;
  }
  public void setEnabled(boolean isEnabled) {
    enabled = isEnabled;
  }
  public boolean isEnabled() {
    return enabled;
  }

  public void set(float x, float y) {
    this.x = Utilities.scale(x, SauceView.controllerWidth, 1);
    this.y = Utilities.scale(y, 1f - SauceView.padHeight, 1);

    handler.updateParameter(x, y);
  }

  public boolean contains(int x, int y) {
    return Math.abs(x - lastX) <= touchRadius &&
            Math.abs(y - lastY) <= touchRadius;
  }
}
