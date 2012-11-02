package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.ClickHandler;
import com.mattfeury.saucillator.dev.android.utilities.VibratorService;

public class LooperTab extends Tab {

  public LooperTab(final AudioEngine engine) {
    super("Looper", engine);
    
    panel.addChild(
      ButtonBuilder
        .build(ButtonBuilder.Type.RECT, "Toggle Looper!")
        .withHandler(new ClickHandler() {
          public void handle(Object o) {
            engine.toggleLooperRecording();
            VibratorService.vibrate();
          }
        })
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.RECT, "Undo")
        .withHandler(new ClickHandler() {
          public void handle(Object o) {
            engine.undoLooper();
            VibratorService.vibrate();
          }
        })
        .withClear(true)
        .finish(),
      ButtonBuilder
        .build(ButtonBuilder.Type.RECT, "Reset")
        .withHandler(new ClickHandler() {
          public void handle(Object o) {
            engine.resetLooper();
            VibratorService.vibrate();
          }
        })
        .withClear(true)
        .finish()
    );
  }
}
