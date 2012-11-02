package com.mattfeury.saucillator.dev.android.templates;

import com.mattfeury.saucillator.dev.android.utilities.ClickHandler;

public class ButtonBuilder {
  private RectButton button;

  private ButtonBuilder(String name) {
    button = new RectButton(name);
  }
  public static ButtonBuilder build(String name) {
    return new ButtonBuilder(name);
  }

  public ButtonBuilder withOnClick(ClickHandler handler) {
    button.addOnClick(handler);
    return this;
  }
  public ButtonBuilder withClear(boolean clear) {
    button.setClear(clear);
    return this;
  }

  public RectButton finish() {
    return button;
  }
}