package com.mattfeury.saucillator.dev.android.templates;


public class ButtonBuilder {
  public static enum Type { RECT, KNOB };
  private Button button;

  private ButtonBuilder(Type type, String name) {
    switch(type) {
      case KNOB:
        button = new KnobButton(name);
        break;
      default:
        button = new RectButton(name);
    }
  }
  public static ButtonBuilder build(Type type, String name) {
    return new ButtonBuilder(type, name);
  }

  public ButtonBuilder withHandler(Handler handler) {
    button.addHandler(handler);
    return this;
  }
  public ButtonBuilder withClear(boolean clear) {
    button.setClear(clear);
    return this;
  }

  public Button finish() {
    return button;
  }
}