package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.templates.Button;
import com.mattfeury.saucillator.dev.android.templates.ButtonBuilder;
import com.mattfeury.saucillator.dev.android.templates.ClickHandler;
import com.mattfeury.saucillator.dev.android.services.VibratorService;

public class LooperTab extends Tab {
  
  private static final int BORDER_SIZE = 5, MARGIN_SIZE = 15;

  public LooperTab(final AudioEngine engine) {
    super("Looper", engine);

    Button firstChild = makeLooperButton("Toggle Looper", new ClickHandler() {
      public void handle(Button button, Object o) {
        engine.toggleLooperRecording();
        VibratorService.vibrate();

        button.toggleFocus();
      }
    });
    firstChild.setClear(false);

    panel.addChild(
      firstChild,
      makeLooperButton("Undo", new ClickHandler() {
        public void handle(Button button, Object o) {
          engine.undoLooper();
          VibratorService.vibrate();
        }
      }),
      makeLooperButton("Reset", new ClickHandler() {
        public void handle(Button button, Object o) {
          engine.resetLooper();
          VibratorService.vibrate();
        }
      })
    );
  }
  
  private Button makeLooperButton(String name, ClickHandler... handlers) {
    ButtonBuilder builder = ButtonBuilder.build(ButtonBuilder.Type.RECT, name);
    
    for (ClickHandler handler : handlers)
      builder.withHandler(handler);

    return
      builder
        .withBorderSize(BORDER_SIZE)
        .withMargin(MARGIN_SIZE)
        .withClear(true)
        .finish();

  }
}
