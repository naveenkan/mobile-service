package com.mobile.fonoapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FonoCacheConfig {
  private final FonoApiProperties fonoApiProperties;

  @Bean
  public CaffeineCache fonoCaffeineCache(Ticker ticker) {
    final var cacheConfig = fonoApiProperties.getCache();
    final var caffeineBuilder =
        Caffeine.newBuilder().ticker(ticker).expireAfterWrite(cacheConfig.getDuration()).build();
    return new CaffeineCache(cacheConfig.getName(), caffeineBuilder);
  }
}
