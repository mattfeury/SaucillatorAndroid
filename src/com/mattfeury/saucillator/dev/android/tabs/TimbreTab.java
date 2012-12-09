package com.mattfeury.saucillator.dev.android.tabs;

import java.util.ArrayList;
import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.instruments.ComplexOsc;
import com.mattfeury.saucillator.dev.android.instruments.Oscillator;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.sound.OscillatorUpdater;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.KnobButton;
import com.mattfeury.saucillator.dev.android.templates.PickerButton;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.templates.RowPanel;
import com.mattfeury.saucillator.dev.android.templates.Table;
import com.mattfeury.saucillator.dev.android.utilities.Utilities;
import com.mattfeury.saucillator.dev.android.services.InstrumentService;
import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.services.ViewService;

public class TimbreTab extends Tab {
  
  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15, TEXT_SIZE = 18;
  private TimbreTable timbreTable;
  
  private static final float HARMONIC_MIN = 1, HARMONIC_MAX = 5, PHASE_MIN = 0, PHASE_MAX = 360;

  public TimbreTab(final AudioEngine engine) {
    super("Timbre", engine);

    final RectButton toggleButton = new RectButton("Add") {
      @Override
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
      }
    };
    //toggleButton.setBorder(BORDER_SIZE);
    //toggleButton.setMargin(MARGIN_SIZE);
    toggleButton.setTextSize(TEXT_SIZE);
    toggleButton.setClear(false);
    
    toggleButton.addHandler(new Handler<Object>() {
      public void handle(Object data) {
        engine.updateOscillatorProperty(new OscillatorUpdater() {
          public void update(ComplexOsc osc) {
            Oscillator newTimbre = InstrumentService.getOscillatorForTimbre("Sine");
            osc.fill(newTimbre);
          }
        });

        timbreTable.fill(AudioEngine.currentOscillator);
      }
    });

    timbreTable = new TimbreTable();
    timbreTable.setRowspan(8);
    timbreTable.setBorder(0);
    timbreTable.setClear(true);
    timbreTable.fill(AudioEngine.getCurrentOscillator());

    panel.addChild(
      toggleButton,
      timbreTable
    );
  }

  public TimbreRow makeTimbreRowFor(Oscillator timbre, int index, String[] disallowedTypes) {
    TimbreRow row = new TimbreRow(timbre, index, disallowedTypes);

    return row;
  }
  
  public class TimbreTable extends Table {
    public TimbreTable() {
      super("timbre-table");
    }

    public void fill(ComplexOsc osc) {
      this.removeChildren();

      LinkedList<Oscillator> timbres = osc.getComponents();
      
      // TODO should this check to see if it's basic or internal? SingingSaw is internal but not basic...
      String[] disallowed = osc.isInternal() ? new String[0] : new String[]{ osc.getName() }; 

      boolean first = true;
      int i = 0;
      for (Oscillator timbre : timbres) {
        TimbreRow row = makeTimbreRowFor(timbre, i++, disallowed);
        if (first) {
          row.setClear(false);
          first = false;
        }

        this.addChild(row);
      }
    }
  }

  private class TimbreRow extends Table {
    public TimbreRow(final Oscillator timbre, final int timbreIndex, String[] disallowedTypes) {
      super("timbre-row-" + timbre.getName());
      this.setClear(true);
      this.setBorder(0);

      ArrayList<String> timbres = InstrumentService.getAllInstrumentNames();
      for (String disallowedType : disallowedTypes) {
        int index = timbres.lastIndexOf(disallowedType);

        if (index > -1)
          timbres.remove(index);
      }

      /**
       * We can't just update properties on this Oscillator object because it likely comes from AudioEngine's currentOscillator
       * which is an oscillator that is never heard, but is an in-memory version of the current oscillator. It is used for creating
       * new oscillators at runtime. Because of this, we use AudioEngine's updateOscillatorProperty which will update the
       * currentOscillator but also update any existing (currently playing) oscillators. 
       */

      String name = timbre.getName();
      PickerButton<String> typePicker = new PickerButton<String>(name, timbres.toArray(new String[timbres.size()]), name);
      typePicker.setColspan(2);
      typePicker.addHandler(new Handler<String>() {
        public void handle(final String type) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator newTimbre = InstrumentService.getOscillatorForTimbre(type);
              newTimbre.setAmplitude(timbre.getAmplitude());
              newTimbre.setHarmonic(timbre.getHarmonic());
              newTimbre.setPhase(timbre.getPhase());
              
              osc.removeComponent(timbreIndex);
              osc.insertComponent(timbreIndex, newTimbre);
            }
          });
        }
      });

      float harmonic = Utilities.unscale(timbre.getHarmonic(), HARMONIC_MIN, HARMONIC_MAX);
      KnobButton harmonicKnob = new KnobButton("Harmonic", harmonic);
      harmonicKnob.addHandler(new Handler<Float>() {
        public void handle(final Float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              timbre.setHarmonic(Utilities.scale(progress, (int)HARMONIC_MIN, (int)HARMONIC_MAX));
            }
          });
        }
      });

      KnobButton amplitudeKnob = new KnobButton("Amplitude", timbre.getAmplitude()); 
      amplitudeKnob.addHandler(new Handler<Float>() {
        public void handle(final Float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              timbre.setAmplitude(progress);
            }
          });
        }
      });

      float phase = Utilities.unscale(timbre.getPhase(), PHASE_MIN, PHASE_MAX);
      KnobButton phaseKnob = new KnobButton("Phase", phase);
      phaseKnob.addHandler(new Handler<Float>() {
        public void handle(final Float progress) {
          engine.updateOscillatorProperty(new OscillatorUpdater() {
            public void update(ComplexOsc osc) {
              Oscillator timbre = osc.getComponent(timbreIndex);
              timbre.setPhase(Utilities.scale(progress, (int)PHASE_MIN, (int)PHASE_MAX));
            }
          });
        }
      });

      this.addChild(typePicker, harmonicKnob, amplitudeKnob, phaseKnob);
    }
  }
}
