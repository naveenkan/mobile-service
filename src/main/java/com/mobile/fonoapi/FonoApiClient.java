package com.mobile.fonoapi;

import com.mobile.exception.FonoApiException;
import com.mobile.fonoapi.response.FonoDeviceInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FonoApiClient {

  private static final String GET_DEVICE_URL = "/getdevice";

  private final RestTemplate restTemplate;

  private final CaffeineCache cache;

  @CircuitBreaker(name = "fono-api", fallbackMethod = "getPhoneInfoFromCache")
  // @Retry(name = "fono-api",fallbackMethod ="getPhoneInfoFromCache" ) # Need to Understand more
  // about combination of CB and Retry pattern
  public List<FonoDeviceInfo> getPhoneSpecification(String deviceName) {
    ResponseEntity<List<FonoDeviceInfo>> responseEntity =
        restTemplate.exchange(
            GET_DEVICE_URL + "?deviceName=" + deviceName,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});
    var deviceInfoList = responseEntity.getBody();
    // update cache with success response
    cache.put(deviceName, deviceInfoList);
    return deviceInfoList;
  }

  private List<FonoDeviceInfo> getPhoneInfoFromCache(String deviceName, Exception e)
      throws Exception {
    if (!(e instanceof HttpClientErrorException)) {
      log.warn(
          "fetching fono api response from cache in fallback,exception msg: {}", e.getMessage());
      return (List<FonoDeviceInfo>)
          cache
              .getNativeCache()
              .get(
                  deviceName,
                  key -> {
                    log.error("no cache found in fallback for device name" + deviceName);
                    throw new FonoApiException("not able to get info from fono api");
                  });
    } else throw e;
  }
}
