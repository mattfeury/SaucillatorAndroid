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
    color.setColor(Color.GREEN);
	}
	
	public void setX(float x)
	{
		this.x = x;
	}
	
	public void setY(float y)
	{
		this.y = y;
	}
	
	public void draw(Canvas canvas)
	{
		canvas.drawCircle(x, y, size * BASE_SIZE, color);
	}

}
