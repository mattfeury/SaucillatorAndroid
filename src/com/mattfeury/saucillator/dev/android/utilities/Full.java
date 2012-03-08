package com.mattfeury.saucillator.dev.android.utilities;

public class Full<K> implements Box<K> {
  private K value;
  public Full(K val) {
    this.value = val;
  }
  @Override
  public K getOrElse(K other) {
    return value;
  }

  public boolean isDefined() {
    return true;
  }
  public boolean isEmpty() {
    return false;
  }
  public boolean isFailure() {
    return false;
  }
  public String getFailure() {
    return "";
  }

}
