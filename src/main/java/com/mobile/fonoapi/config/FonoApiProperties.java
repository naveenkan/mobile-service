package com.mobile.fonoapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties("fono-api")
@Data
@Configuration
public class FonoApiProperties {
  private String baseUrl;
  private Cache cache;

  @Data
  public static class Cache {
    private String name;
    private Duration duration;
  }
}
