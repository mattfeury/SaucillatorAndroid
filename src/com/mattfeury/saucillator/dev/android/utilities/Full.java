package com.mattfeury.saucillator.dev.android.utilities;

public class Full<K> implements Box<K> {
  private K value;
  public Full(K val) {
    this.value = val;
  }
  @Override
  public K openOr(K other) {
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

  public <R> Box<R> map(MapFunc<K, R> funcHolder) {
    return funcHolder.func(value);
  }
  public void foreach(EachFunc<K> funcHolder) {
    funcHolder.func(value);
  }
}
