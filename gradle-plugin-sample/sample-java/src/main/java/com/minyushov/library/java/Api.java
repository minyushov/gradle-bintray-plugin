package com.minyushov.library.java;

import io.reactivex.Completable;

/**
 * Api class
 */
public class Api {
  /**
   * Returns {@link Completable} completable
   *
   * @return {@link Completable} completable
   */
  public Completable action() {
    return Completable.complete();
  }
}