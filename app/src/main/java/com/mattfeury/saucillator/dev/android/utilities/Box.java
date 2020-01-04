package com.mattfeury.saucillator.dev.android.utilities;

public interface Box<K> {
  public K openOr(K other);
  
  public boolean isDefined();
  public boolean isEmpty();
  public boolean isFailure();
  public String getFailure();
  
  public <R> Box<R> map(MapFunc<K, R> funcHolder);

  public void foreach(EachFunc<K> funcHolder);
}
