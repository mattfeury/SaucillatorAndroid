package com.mattfeury.saucillator.dev.android.utilities;

public interface MapFunc<K, R> {
  public Box<R> func(K k);
}
