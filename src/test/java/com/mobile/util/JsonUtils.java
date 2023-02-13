package com.mobile.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {


  public static <T> T readValueAsObject(String location, Class<T> targetClass) throws IOException {
    var inStream = JsonUtils.class.getResourceAsStream(location);
    return new ObjectMapper().readValue(inStream, targetClass);
  }
}
