package com.mattfeury.saucillator.dev.android.visuals;


import android.graphics.*;

public class FractalGen {
	
	float radius;
	Paint paint;
	Canvas canvas;
	float scale;
	
	public FractalGen(float radius, Paint paint, Canvas canvas){
		this.radius = radius;
		this.paint = paint;
		this.canvas = canvas;
		scale = 1.5f;
	}
	
	public FractalGen(Canvas canvas){
		this.canvas = canvas;
		paint = new Paint();
		paint.setColor(Color.RED);
		scale = 1.5f;
		radius = 2.0f;
	}
	
	public float toScreen(float in, boolean isWidth){
		float newScale = 3.0f;
		return ((in + newScale/2.0f) / newScale) * (isWidth ? canvas.getWidth() : canvas.getHeight());
		
	}
	
	public float toInput(float in, boolean isWidth){
		return (((in / (isWidth ? canvas.getWidth() : canvas.getHeight())) * scale) - scale/2.0f);
		
		
	}
	
	void drawDatCircle(float x, float y){
		float screenX = toScreen(x, true);
		float screenY = toScreen(y, false);
		
		if(screenX > canvas.getWidth() || screenY > canvas.getHeight())
			return;

		canvas.drawCircle(screenX, screenY, radius, paint);
	}
	
	void drawFractal (ComplexNum base, ComplexNum current, int depth){
		   if (depth >8)
		    return;

		   drawDatCircle(current.getReal(), current.getImag());
		   
		   
		   ComplexNum nextPow = base.pow(depth + 1);
		   ComplexNum nextPlus = new ComplexNum(current.getReal() + nextPow.getReal(), current.getImag() + nextPow.getImag());
		   ComplexNum nextMinus = new ComplexNum(current.getReal() - nextPow.getReal(), current.getImag() - nextPow.getImag());

		   drawFractal(base, nextPlus, depth + 1);
		   drawFractal(base, nextMinus, depth + 1);
		   
		}
	
}
