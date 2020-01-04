package com.mattfeury.saucillator.dev.android.tabs;

import java.util.Arrays;

import com.mattfeury.saucillator.dev.android.SauceEngine;
import com.mattfeury.saucillator.dev.android.instruments.Theory;
import com.mattfeury.saucillator.dev.android.instruments.Theory.Scale;
import com.mattfeury.saucillator.dev.android.services.ViewService;
import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.PickerButton;

public class PadTab extends Tab {
  
  private static final int BORDER_SIZE = 8, MARGIN_SIZE = 15;

  public PadTab(final AudioEngine engine) {
    super("Pad", "Pad Settings", engine);

    PickerButton<Scale> scalePicker = new PickerButton<Scale>("Scale", Theory.scales);
    scalePicker.addHandler(new Handler<Scale>() {
      public void handle(Scale scale) {
        engine.setScaleById(scale.toString());
      }
    });

    PickerButton<String> baseNotePicker = new PickerButton<String>("Base Note", Theory.notes);
    baseNotePicker.addHandler(new Handler<String>() {
      public void handle(String note) {
        int index = Arrays.asList(Theory.notes).indexOf(note);
        engine.updateBaseNote(index);
      }
    });
    baseNotePicker.setClear(true);

    PickerButton<Integer> baseOctavePicker = new PickerButton<Integer>("Base Octave", Theory.octaves, (Integer)4);
    baseOctavePicker.addHandler(new Handler<Integer>() {
      public void handle(Integer octave) {
        engine.updateBaseOctave(octave);
      }
    });

    panel.addChild(
      scalePicker,
      ButtonBuilder
        .build(ButtonBuilder.Type.SLIDER, "Grid Size")
        .withHandler(new Handler<Integer>() {
          public void handle(Integer value) {
            SauceEngine.TRACKPAD_GRID_SIZE = value;
          }
        })
        .withBorderSize(BORDER_SIZE)
        .withMargin(MARGIN_SIZE)
        .withBounds(SauceEngine.TRACKPAD_SIZE_MIN, SauceEngine.TRACKPAD_SIZE_MAX, SauceEngine.TRACKPAD_GRID_SIZE)
        .withClear(true)
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.TOGGLE, "Show/Hide Grid")
        .withHandler(new Handler<Boolean>() {
          public void handle(Boolean show) {
            ViewService.toggleShowGrid();
          }
        })
        .withMargin(MARGIN_SIZE)
        .withFocus(ViewService.isGridShowing())
        .withClear(true)
        .finish(),
       baseNotePicker,
       baseOctavePicker
    );
  }  
}
