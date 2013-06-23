package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.services.VibratorService;
import com.mattfeury.saucillator.dev.android.services.ViewService;

public class LooperTab extends Tab {
  
  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15, TEXT_SIZE = 18;

  public LooperTab(final AudioEngine engine) {
    super("Looper", engine);

    final RectButton toggleButton = new RectButton("Toggle Loop Record") {
      @Override
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
        engine.toggleLooperRecording();
        Button loopTab = ViewService.getButton("Looper");
        ((TabSelector)loopTab).toggleAlert();

        toggleFocus();
      }
    };
    toggleButton.setBorder(BORDER_SIZE);
    toggleButton.setMargin(MARGIN_SIZE);
    toggleButton.setTextSize(TEXT_SIZE);
    toggleButton.setClear(false);

    RectButton undoButton = new RectButton("Undo Last Loop") {
      @Override
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
        engine.undoLooper();

        // Undo stops any current loop recording
        toggleButton.setFocus(false);
        Button loopTab = ViewService.getButton("Looper");
        ((TabSelector)loopTab).setAlert(false);
      }
    };
    undoButton.setBorder(BORDER_SIZE);
    undoButton.setMargin(MARGIN_SIZE);
    undoButton.setTextSize(TEXT_SIZE);
    undoButton.setClear(true);

    RectButton resetButton = new RectButton("Reset/Clear Looper") {
      @Override
      public void handle(Object o) {
        super.handle(o);

        VibratorService.vibrate();
        engine.resetLooper();

        // Reset stops any current loop recording
        toggleButton.setFocus(false);
        Button loopTab = ViewService.getButton("Looper");
        ((TabSelector)loopTab).setAlert(false);
      }
    };
    resetButton.setBorder(BORDER_SIZE);
    resetButton.setMargin(MARGIN_SIZE);
    resetButton.setTextSize(TEXT_SIZE);
    resetButton.setClear(true);

    panel.addChild(
      toggleButton,
      undoButton,
      resetButton
    );
  }
}
