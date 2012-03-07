package com.mattfeury.saucillator.android.utilities;

public class Empty<K> implements Box<K> {
  public Empty() {
  }
  @Override
  public K getOrElse(K other) {
    return other;
  }

  public boolean isDefined() {
    return false;
  }
  public boolean isEmpty() {
    return true;
  }
  public boolean isFailure() {
    return false;
  }
  public String getFailure() {
    return "";
  }

}
