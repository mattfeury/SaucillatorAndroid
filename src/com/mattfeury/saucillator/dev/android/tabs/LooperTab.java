package com.mattfeury.saucillator.dev.android.tabs;

import com.mattfeury.saucillator.dev.android.sound.AudioEngine;
import com.mattfeury.saucillator.dev.android.visuals.RectButton;

public class LooperTab extends Tab {

  public LooperTab(final AudioEngine engine) {
    super("Looper", engine);
    
    panel.addChild(new RectButton("Toggle Looper!") {
      @Override
      public void click() {
        engine.toggleLooperRecording();
      }
      @Override
      public boolean shouldClearFloat() {
        return true;
      }
    });
    panel.addChild(new RectButton("Undo") {
      @Override
      public void click() {
        engine.undoLooper();
      }
      @Override
      public boolean shouldClearFloat() {
        return true;
      }
    });
    panel.addChild(new RectButton("Reset") {
      @Override
      public void click() {
        engine.resetLooper();
      }
      @Override
      public boolean shouldClearFloat() {
        return true;
      }
    });
  }
}
