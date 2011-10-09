package com.mattfeury.saucillator.android;

import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SauceView extends View {

    //for the controller
    public static final float controllerWidth = .15f; //percentage
    public static final float padWidth = 1.0f - controllerWidth;
    public static final int numButtons = 2;
    
    private boolean init = false;

    //graphics elements
    private HashMap<Integer, Finger> fingers = new HashMap<Integer, Finger>();
    private RectButton loop, reset;
    FractalGen fractGen;
    float fX = 0, fY = 0; //fractal x and y coords
    Paint backColor;
    private boolean visuals = false;

    public SauceView(Context context) {
      super(context);
      init(context);
    }
    public SauceView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
    }
    public SauceView(Context context, AttributeSet attrs, int defStyle) {
    	 super(context, attrs, defStyle);
    	 init(context);
    }

    public void init(Context context) {
      backColor = new Paint(Color.GREEN);
    }

    public void setVisuals(boolean show) {
      visuals = show;
      invalidate();
    }
    public boolean getVisuals() {
      return visuals;
    }

    public boolean isInPad(float x, float y) {
      return x > (getMeasuredWidth() * controllerWidth);
    }

    public void updateOrCreateFinger(int id, float x, float y, float size, float pressure) {
      Finger maybe = fingers.get((Integer)id);
      if (maybe != null) {
        maybe.update(x, y, size, pressure);
      } else {
        Finger f = new Finger(id, x, y, size, pressure);
        fingers.put((Integer)id, f);
      }
      invalidate();
    }

    public void clearFingers() {
      fingers.clear();
      invalidate();
    }
    public void removeFinger(int id) {
      fingers.remove((Integer)id);
      invalidate();    
    }
 
    @Override
    public void onDraw(Canvas canvas) {    	
    	//draw pad
      if (fractGen == null)
        fractGen = new FractalGen(canvas);

      fX = (fingers.values().size() > 0 ? 0 : fX);
      fY = (fingers.values().size() > 0 ? 0 : fY);

      for(Finger f : fingers.values()){
        if (f.id == 0) {
          fX += f.x;
          fY += f.y;
        } else {
          backColor.setColor(Color.HSVToColor(new float[]{(f.x / canvas.getWidth())* 360, f.y / canvas.getHeight(), f.y / canvas.getHeight()}));
          fractGen.paint.setColor(Color.HSVToColor(new float[]{360 - (f.x / canvas.getWidth()* 360), 1f - f.y / canvas.getHeight(), 1f - f.y / canvas.getHeight()}));
        }
      }
      
      fX /= (fingers.values().size() > 0 ? fingers.values().size() : 1);
      fY /= (fingers.values().size() > 0 ? fingers.values().size() : 1);
      
      
      canvas.drawColor(backColor.getColor());
      
      if(visuals)		
        fractGen.drawFractal(new ComplexNum(fractGen.toInput(fX, true), fractGen.toInput(fY, false)), new ComplexNum(0,0), -1);
      
      for(Finger f : fingers.values())
        f.draw(canvas);

      //draw controller
    	if (! init) {
        loop = new RectButton("Loop", 0, 0, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        reset = new RectButton("Reset", 0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        init = true;
    	} //else {
        //loop.set(0, 0, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        //reset.set(0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);    		
    	//}

      loop.draw(canvas);
      reset.draw(canvas);
    }

    class RectButton extends RectF {
      private Paint bg,text;
      private String name;
      public RectButton(String name, int x, int y, int width, int height) {
        super(x, y, x + width, y + height);
        this.name = name;
        bg = new Paint();
        text = new Paint();
        Random rnd = new Random();

        bg.setARGB(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        text.setARGB(255, 0,0,0);
      }
      public void draw(Canvas canvas) {
        canvas.drawRect(this, bg);
        canvas.drawText(name, left + right * .3f, top + bottom * .4f, text);
      }
    }

}
