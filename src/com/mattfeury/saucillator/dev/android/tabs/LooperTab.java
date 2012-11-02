package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.utilities.ClickHandler;

public class LooperTab extends Tab {

  public LooperTab(final AudioEngine engine) {
    super("Looper", engine);
    
    panel.addChild(
      ButtonBuilder
        .build("Toggle Looper!")
        .withOnClick(new ClickHandler() {
          public void onClick() {
            engine.toggleLooperRecording();
          }
        })
        .finish(),
      ButtonBuilder
        .build("Undo")
        .withOnClick(new ClickHandler() {
          public void onClick() {
            engine.undoLooper();
          }
        })
        .withClear(true)
        .finish(),
      ButtonBuilder
        .build("Reset")
        .withOnClick(new ClickHandler() {
          public void onClick() {
            engine.resetLooper();
          }
        })
        .withClear(true)
        .finish()
    );
  }
}
