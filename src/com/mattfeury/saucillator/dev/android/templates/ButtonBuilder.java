package com.mattfeury.saucillator.dev.android.templates;


public class ButtonBuilder {
  public static enum Type { RECT, KNOB, SLIDER, TOGGLE };
  private Button button;

  private ButtonBuilder(Type type, String name) {
    switch(type) {
      case KNOB:
        button = new KnobButton(name);
        break;
      case SLIDER:
        button = new SliderButton(name);
        break;
      case TOGGLE:
        button = new ToggleButton(name);
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
  public ButtonBuilder withBorderSize(int size) {
    button.setBorder(size);
    return this;
  }
  public ButtonBuilder withMargin(int size) {
    button.setMargin(size);
    return this;
  }
  public ButtonBuilder withFocus(boolean focused) {
    button.setFocus(focused);
    return this;
  }
  public ButtonBuilder withProgress(float progress) {
    if (button instanceof KnobButton)
      ((KnobButton)button).changeProgress(progress);

    return this;
  }

  public ButtonBuilder withBounds(int min, int max) {
    return withBounds(min, max, min);
  }
  public ButtonBuilder withBounds(int min, int max, int current) {
    if (button instanceof SliderButton)
      ((SliderButton)button).setBounds(min, max, current);

    return this;
  }
  public ButtonBuilder withTextSize(int textSize) {
    button.setTextSize(textSize);
    return this;
  }

  public Button finish() {
    return button;
  }
}