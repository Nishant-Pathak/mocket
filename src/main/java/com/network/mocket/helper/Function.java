package com.network.mocket.helper;

public interface Function<T, R> {
  R apply(T t);
}
