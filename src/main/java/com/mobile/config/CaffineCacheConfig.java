package com.mobile.config;

import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@EnableCaching
public class CaffineCacheConfig {
  @Bean
  public CacheManager cacheManager(Set<CaffeineCache> caffeineCaches) {
    final var caffeineCacheManager = new SimpleCacheManager();
    caffeineCacheManager.setCaches(caffeineCaches);
    return caffeineCacheManager;
  }

  @Bean
  public Ticker ticker() {
    return Ticker.systemTicker();
  }
}
