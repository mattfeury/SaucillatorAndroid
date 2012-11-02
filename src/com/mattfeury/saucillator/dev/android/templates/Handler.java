package com.mattfeury.saucillator.dev.android.templates;

public interface Handler<K> {
  public void handle(Button button, K data);
}
