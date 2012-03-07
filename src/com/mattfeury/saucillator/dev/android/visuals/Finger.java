package com.mattfeury.saucillator.dev.android.visuals;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class Finger {
  int id;
  float x,y,size,pressure;
  float PRESSURE = 0.2f; //defaults because some screens don't report these and hence they won't be visible
  float SIZE = 0.1f;
  Paint color = new Paint();
  public final static int BASE_SIZE = 250;

  public Finger(int id, float x, float y, float size, float pressure)
  {
    this.id = id;
    this.update(x, y, size, pressure);
    
    //Visualizer stuff
    if (id == 0) {
      color.setColor(Color.GREEN);
    } else
      color.setColor(Color.CYAN);
  }

  public void setX(float x) {
    this.x = x;
  }
	
  public void setY(float y) {
    this.y = y;
  }
  
  public float getX() {
	  return x;
  }
  
  public float getY() {
	  return y;
  }

  public void update(float x, float y, float size, float pressure) {
    this.x = x;
    this.y = y;
    this.size = Math.max(SIZE, size);
    this.pressure = Math.max(PRESSURE, pressure);
  }

  public void draw(Canvas canvas) {
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    int pitchColor = Color.argb((int)(500 * pressure),(int)Math.floor((x/(float)width)*255), 255 - (int)Math.floor((y/(float)height)*255), 0);
    color.setColor(pitchColor);

    canvas.drawCircle(x, y, size * BASE_SIZE, color);
  }

}
