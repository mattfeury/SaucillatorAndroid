package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.Handler;
import com.mattfeury.saucillator.dev.android.templates.RectButton;
import com.mattfeury.saucillator.dev.android.services.VibratorService;

public class LooperTab extends Tab {
  
  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15, TEXT_SIZE = 18;

  public LooperTab(final AudioEngine engine) {
    super("Looper", engine);

    RectButton toggleButton = new RectButton("Toggle Loop Record") {
      @Override
      public void handle(Object o) {
        VibratorService.vibrate();
        engine.toggleLooperRecording();

        toggleFocus();
      }
    };
    toggleButton.setBorder(BORDER_SIZE);
    toggleButton.setMargin(MARGIN_SIZE);
    toggleButton.setClear(false);
    toggleButton.setTextSize(TEXT_SIZE);

    panel.addChild(
      toggleButton,
      makeLooperButton("Undo", new Handler<Boolean>() {
        public void handle(Boolean o) {
          engine.undoLooper();
          VibratorService.vibrate();
        }
      }),
      makeLooperButton("Reset", new Handler<Boolean>() {
        public void handle(Boolean o) {
          engine.resetLooper();
          VibratorService.vibrate();
        }
      })
    );
  }
  
  private Button makeLooperButton(String name, Handler... handlers) {
    ButtonBuilder builder = ButtonBuilder.build(ButtonBuilder.Type.RECT, name);
    
    for (Handler handler : handlers)
      builder.withHandler(handler);

    return
      builder
        .withBorderSize(BORDER_SIZE)
        .withMargin(MARGIN_SIZE)
        .withTextSize(TEXT_SIZE)
        .withClear(true)
        .finish();

  }
}
