package com.mobile.exception;

public class PhoneNotCheckedOutException extends RuntimeException {
  public PhoneNotCheckedOutException(String message) {
    super(message);
  }
}
