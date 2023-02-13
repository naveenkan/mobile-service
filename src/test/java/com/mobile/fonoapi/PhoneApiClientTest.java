package com.mobile.fonoapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mobile.exception.FonoApiException;
import com.mobile.fonoapi.response.FonoDeviceInfo;
import com.mobile.repository.PhoneRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@EnableAutoConfiguration(
    exclude = {
      DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class,
      HibernateJpaAutoConfiguration.class
    })
public class PhoneApiClientTest {

  private static WireMockServer wireMockServer;
  @Autowired protected CircuitBreakerRegistry circuitBreakerRegistry;
  @Autowired private CaffeineCache caffeineCache;
  @Autowired private FonoApiClient fonoApiClient;
  @MockBean private PhoneRepository phoneRepository;

  @BeforeAll
  static void setUp() {
    wireMockServer = new WireMockServer(9999);
    wireMockServer.start();
    configureFor(9999);
  }

  @AfterAll
  static void cleanUp() {
    wireMockServer.stop();
  }

  @Test
  public void getInfoFromFonoApi() {
    // Given
    circuitBreakerRegistry.circuitBreaker("fono-api").transitionToClosedState();
    String deviceName = "SamsungGalaxyS9";

    wireMockServer.stubFor(
        get("/v1/getdevice?deviceName=" + deviceName)
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBodyFile("fono-api.json")
                    .withHeader("content-type", "application/json")));

    // When
    List<FonoDeviceInfo> FonoDeviceInfos = fonoApiClient.getPhoneSpecification(deviceName);

    // Then
    Assertions.assertThat(FonoDeviceInfos)
        .extracting(FonoDeviceInfo::getTechnology)
        .containsExactly("GSM");
    Assertions.assertThat(caffeineCache.getNativeCache().getIfPresent(deviceName))
        .isEqualTo(FonoDeviceInfos);
    verify(getRequestedFor(urlEqualTo("/v1/getdevice?deviceName=" + deviceName)));
  }

  @Test
  public void getInfoFromCacheWhenFonoApiIsDown() {
    // Given
    circuitBreakerRegistry.circuitBreaker("fono-api").transitionToClosedState();
    String deviceName = "i9305";
    wireMockServer.stubFor(
        WireMock.get("/v1/getdevice?deviceName=" + deviceName).willReturn(serviceUnavailable()));

    caffeineCache.put(deviceName, List.of(FonoDeviceInfo.builder().technology("GSM").build()));

    // When
    var FonoDeviceInfoList = fonoApiClient.getPhoneSpecification(deviceName);

    // Then
    Assertions.assertThat(FonoDeviceInfoList)
        .hasSize(1)
        .extracting(FonoDeviceInfo::getTechnology)
        .containsExactly("GSM");
    verify(getRequestedFor(urlEqualTo("/v1/getdevice?deviceName=" + deviceName)));
  }

  @Test
  public void getInfoFromCacheWhenCircuitBreakerInOpenState() {
    // Given
    circuitBreakerRegistry.circuitBreaker("fono-api").transitionToOpenState();
    String deviceName = "i9305";

    caffeineCache.put(deviceName, List.of(FonoDeviceInfo.builder().technology("GSM").build()));

    // When
    var FonoDeviceInfoList = fonoApiClient.getPhoneSpecification(deviceName);

    // Then
    Assertions.assertThat(FonoDeviceInfoList)
        .hasSize(1)
        .extracting(FonoDeviceInfo::getTechnology)
        .containsExactly("GSM");
  }

  @Test
  public void ShouldThrowExceptionForBadRequest() {
    // Given
    circuitBreakerRegistry.circuitBreaker("fono-api").transitionToClosedState();
    String deviceName = "i9305";

    wireMockServer.stubFor(get("/v1/getdevice?deviceName=" + deviceName).willReturn(status(400)));

    // When && Then
    Assertions.assertThatThrownBy(() -> fonoApiClient.getPhoneSpecification(deviceName))
        .isInstanceOf(HttpClientErrorException.class);
  }

  @Test
  public void ShouldThrowExceptionWhenDataIsNotFoundInCache() {
    // Given
    circuitBreakerRegistry.circuitBreaker("fono-api").transitionToClosedState();
    String deviceName = "i9305";
    caffeineCache.clear();

    wireMockServer.stubFor(
        WireMock.get("/v1/getdevice?deviceName=" + deviceName).willReturn(serviceUnavailable()));

    // When && Then
    Assertions.assertThatThrownBy(() -> fonoApiClient.getPhoneSpecification(deviceName))
        .isInstanceOf(FonoApiException.class)
        .hasMessageContaining("not able to get info from fono api");
  }
}
