package com.mattfeury.saucillator.android.visuals;

import java.util.HashMap;
import java.util.Random;
import java.util.LinkedList;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SauceView extends View {

    //for the controller
    public static final float controllerWidth = .15f; //percentage
    public static final float padWidth = 1.0f - controllerWidth;
    public static final int numButtons = 3;
    
    private boolean init = false;

    //graphics elements
    private HashMap<Integer, Finger> fingers = new HashMap<Integer, Finger>();
    private RectButton loop, undo, reset;
    private LinkedList<DrawableParameter> params = new LinkedList<DrawableParameter>();

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
      loop = new RectButton("Loop", 0, 0, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
      undo = new RectButton("Undo", 0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
      reset = new RectButton("Reset", 0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
    }

    public void addParam(DrawableParameter p) {
      if (! params.contains(p))
        params.add(p);
    }
    public void resetParams() {
      params.clear();
    }
    public DrawableParameter optParameter(float x, float y, Object preferred) {
      // A list of all params at this location. If there are multiple, we prefer 'preferred'
      LinkedList<DrawableParameter> contained = new LinkedList<DrawableParameter>();

      for (DrawableParameter p : params)
        if (p.contains((int)x, (int)y))
          contained.add(p);

      int size = contained.size();
      if (size == 0)
        return null;
      else if (size == 1)
        return contained.getFirst();
      else if (contained.contains(preferred))
        return (DrawableParameter) preferred;
      else
        return contained.getLast();
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
    public void focusLooper() {
      loop.focus();
    }
    public void unfocusLooper() {
      loop.unfocus();
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

      // Draw controller
      // We must reset the button dimensions in this event because they are not accurate on load time.
      if (! init) {
        loop.set(0, 0, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        undo.set(0, getHeight() / numButtons, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        reset.set(0, getHeight() / numButtons * 2, (int) (getWidth() * controllerWidth), getHeight() / numButtons);
        init = true;
      }

      loop.draw(canvas);
      undo.draw(canvas);
      reset.draw(canvas);

      for (DrawableParameter p : params)
        p.draw(canvas);
    }

    class RectButton extends RectF {
      private Paint bg,text,focusedBg;
      private int borderWidth = 5;
      private String name;
      private boolean focused = false;
      public RectButton(String name, int x, int y, int width, int height) {
        super(x, y, x + width, y + height);
        this.name = name;
        bg = new Paint();
        focusedBg = new Paint();
        text = new Paint();
        Random rnd = new Random();

        bg.setARGB(100, 242,204,133);
        focusedBg.setARGB(255, 155 + rnd.nextInt(100), 0, 0);
        text.setARGB(255, 255,255,255);
        
        bg.setStrokeWidth(5);

      }
      public void set(int x, int y, int width, int height) {
        super.set(x, y, x + width, y + height);      	
      }
      public void draw(Canvas canvas) {
        if (focused) {
          canvas.drawRect(left, top, right, top + borderWidth, focusedBg); //top line
          //canvas.drawRect(left, top, left + borderWidth, bottom - borderWidth, focusedBg); //left line
          //canvas.drawRect(left, bottom - borderWidth, right, bottom, focusedBg); //bottom line
          //canvas.drawRect(right - borderWidth, top, right, bottom - borderWidth, focusedBg); //right line
          canvas.drawText(name, left + right * .3f, top + (bottom - top)* .5f, focusedBg);
        } else {
          canvas.drawRect(left + borderWidth, top, right - borderWidth, top + borderWidth, bg); //top line
          canvas.drawRect(left, top, left + borderWidth, bottom - borderWidth, bg); //left line
          canvas.drawRect(left, bottom - borderWidth, right, bottom, bg); //bottom line
          canvas.drawRect(right - borderWidth, top, right, bottom - borderWidth, bg); //right line
          
          canvas.drawText(name, left + right * .3f, top + (bottom - top)* .5f, text);
        }
      }
      public void focus() {
      	focused = true;
      	invalidate();
      }
      public void unfocus() {
      	focused = false;
      	invalidate();
      }
      
    }
}
