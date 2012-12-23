package com.mattfeury.saucillator.dev.android.tabs;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.sound.ParametricEQ;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.templates.ColumnPanel;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.services.ViewService;

public class EqTab extends Tab {
  
  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15, TEXT_SIZE = 18;

  public EqTab(final AudioEngine engine) {
    super("EQ", engine);

    final ParametricEQ eq = engine.getEq();
    final EqPanel eqPanel = new EqPanel(eq);
    eqPanel.setRowspan(3);

    panel.addChild(
      eqPanel,
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Freq")
        .withProgress(Utilities.unscale(eq.getFrequency(), ParametricEQ.minFreq, ParametricEQ.maxFreq))
        .withHandler(new Handler<Float>() {
          public void handle(final Float progress) {
            int frequency = (int) Utilities.scale(progress, ParametricEQ.minFreq, ParametricEQ.maxFreq);
            eqPanel.setFrequency(frequency);
            eq.setFrequency(frequency);
          }
        })
        .withClear(true)
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Q")
        .withProgress(Utilities.unscale(eq.getQ(), ParametricEQ.minQ, ParametricEQ.maxQ))
        .withHandler(new Handler<Float>() {
          public void handle(final Float progress) {
            float q = Utilities.scale(progress, ParametricEQ.minQ, ParametricEQ.maxQ);
            eqPanel.setQ(q);
            eq.setQ(q);
          }
        })
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.KNOB, "Gain")
        .withProgress(Utilities.unscale(eq.getGain(), ParametricEQ.minGain, ParametricEQ.maxGain))
        .withHandler(new Handler<Float>() {
          public void handle(final Float progress) {
            float gain = Utilities.scale(progress, ParametricEQ.minGain, ParametricEQ.maxGain);
            eqPanel.setGain(gain);
            eq.setGain(gain);
          }
        })
        .finish()

    );
  }

  class EqPanel extends ColumnPanel {
    private Path path;
    private Paint eqPaint;

    private int frequency;
    private float q, gain;
    
    private float midY = (bottom - top) / 2f;

    private static final int drawIncrement = 1;

    public EqPanel(ParametricEQ eq) {
      this.frequency = (int) eq.getFrequency();
      this.q = eq.getQ();
      this.gain = eq.getGain();

      path = new Path();
      //path.setFillType(Path.FillType.EVEN_ODD);
      
      eqPaint = new Paint();
      eqPaint.setARGB(255, 28, 171, 11);

      recalculatePath();
    }
    
    public void setFrequency(int frequency) {
      this.frequency = frequency;
      recalculatePath();
    }
    public void setQ(float q) {
      this.q = q;
      recalculatePath();
    }
    public void setGain(float gain) {
      this.gain = gain;
      recalculatePath();
    }

    @Override
    public void set(int x, int y, int width, int height) {
      super.set(x, y, width, height);

      midY = (bottom - top) / 2f;
      recalculatePath();
    }

    private void recalculatePath() {
      // Quadratic equation in form A*(x - h)^2 + B*x + C
      float a = this.q,
            b = 0,
            c = (bottom - top) / 2f * (this.gain / ParametricEQ.maxGain),
            h = (right - left) * (this.frequency / 20000f) + left;

      float discriminant = (float) (-4 * a * c); 
      float root1 = (float) (Math.sqrt(discriminant) / (2f * a)) + h; 
      float root2 = (float) (-Math.sqrt(discriminant) / (2f * a)) + h;

      path.reset();
      path.moveTo(left, midY);

      if (discriminant > 0) {
        float minRoot = (float) Math.ceil(Math.min(root1, root2));
        float maxRoot = (float) Math.floor(Math.max(root1, root2));

        // This is backwards since y values on the canvas go start at 0 and increment as you go down
        float orientation = (c > 0) ? -1 : 1;

        path.lineTo(minRoot, midY);

        for (int x = (int) minRoot; x < maxRoot; x = x + drawIncrement) {
          float y = midY - (orientation * a * (float)Math.pow(x - h, 2) + c);
          path.lineTo(x, y);
        }

        path.lineTo(maxRoot, midY);
      }

      path.lineTo(right, midY);
      path.lineTo(right, midY - 1);
      path.lineTo(left, midY - 1);
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.drawPath(path, eqPaint);
    }
  }
}
