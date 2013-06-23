package com.mattfeury.saucillator.dev.android.utilities;

public class Empty<K> implements Box<K> {
  public Empty() {
  }
  @Override
  public K openOr(K other) {
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
  public <R> Box<R> map(MapFunc<K, R> funcHolder) {
    return new Empty<R>();
  }
  public void foreach(EachFunc<K> funcHolder) {
    // Noop
  }

}
