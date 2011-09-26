package com.mattfeury.saucillator.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class Finger {
  int id;
  float x,y,size,pressure;
  Paint color = new Paint();
  public final static int BASE_SIZE = 250;

  public Finger(int id, float x, float y, float size, float pressure)
  {
    this.id = id;
    this.x = x;
    this.y = y;
    this.size = size;
    this.pressure = pressure;
    
    //Visualizer stuff
    

    if (id == 0){
      color.setColor(Color.GREEN);
    }
    
    else
      color.setColor(Color.CYAN);
  }

  public void setX(float x)
  {
    this.x = x;
  }
	
  public void setY(float y)
  {
    this.y = y;
  }
  
  public float getX(){
	  return x;
  
  }
  
  public float getY(){
	  return y;
  }

  public void update(float x, float y, float size, float pressure) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.pressure = pressure;
  }

  public void draw(Canvas canvas) {
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    int pitchColor = Color.argb((int)(255 * pressure),(int)Math.floor((x/(float)width)*255), 255 - (int)Math.floor((y/(float)height)*255), 0);
    color.setColor(pitchColor);

    canvas.drawCircle(x, y, size * BASE_SIZE, color);
  }

}
