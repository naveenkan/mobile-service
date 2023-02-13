package com.mobile.exception;

import org.springframework.web.client.HttpClientErrorException;

import java.util.function.Predicate;

public class FonoApiRecordFailurePredicate implements Predicate<Throwable> {
  @Override
  public boolean test(Throwable throwable) {
    return !(throwable instanceof HttpClientErrorException);
  }
}
