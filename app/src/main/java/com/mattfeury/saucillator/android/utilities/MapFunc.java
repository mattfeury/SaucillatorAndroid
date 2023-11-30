package com.mattfeury.saucillator.android.utilities;

public interface MapFunc<K, R> {
  public Box<R> func(K k);
}
