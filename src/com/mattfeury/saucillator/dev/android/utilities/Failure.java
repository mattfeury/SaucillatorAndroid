package com.mattfeury.saucillator.dev.android.utilities;

public class Failure<K> implements Box<K> {
  private String error;
  public Failure(String val) {
    this.error = val;
  }
  @Override
  public K openOr(K other) {
    return other;
  }

  public boolean isDefined() {
    return false;
  }
  public boolean isEmpty() {
    return false;
  }
  public boolean isFailure() {
    return true;
  }
  public String getFailure() {
    return error;
  }

  public <R> Box<R> map(MapFunc<K, R> funcHolder) {
    return new Failure<R>(error);
  }
  public void foreach(EachFunc<K> funcHolder) {
    // Noop
  }

}
