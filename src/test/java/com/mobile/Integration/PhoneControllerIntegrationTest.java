package com.mobile.Integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mobile.model.Phone;
import com.mobile.model.PhoneSpecification;
import com.mobile.repository.PhoneRepository;
import com.mobile.repository.entity.PhoneEntity;
import com.mobile.util.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PhoneControllerIntegrationTest {
  private static final String BASE_API_URL = "/api/v1/phones";

  private static final String SLASH = "/";
  private static WireMockServer wireMockServer;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Autowired private PhoneRepository phoneRepository;

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
  void shouldGetPhoneSpecification() throws Exception {
    // Given
    String deviceName = "SamsungGalaxyS9";
    wireMockServer.stubFor(
        WireMock.get(urlPathMatching("/v1/getdevice"))
            .withQueryParam("deviceName", equalTo(deviceName))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBodyFile("fono-api.json")
                    .withHeader("content-type", "application/json")));

    // When
    RequestBuilder request =
        MockMvcRequestBuilders.get(BASE_API_URL + "/" + deviceName + "/specs")
            .contentType(MediaType.APPLICATION_JSON);

    String responseAsString =
        mockMvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    var phoneSpecs = objectMapper.readValue(responseAsString, PhoneSpecification.class);

    Assertions.assertThat(phoneSpecs)
        .isEqualTo(
            JsonUtils.readValueAsObject("/expected/phone-spec.json", PhoneSpecification.class));
  }

  @Test
  void shouldFetchAllPhones() throws Exception {
    // When
    RequestBuilder request =
        MockMvcRequestBuilders.get(BASE_API_URL).contentType(MediaType.APPLICATION_JSON);

    String responseAsString =
        mockMvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    var phones = objectMapper.readValue(responseAsString, new TypeReference<List<Phone>>() {});

    Assertions.assertThat(phones).hasSize(9);
  }

  @Test
  void shouldGetPhoneByName() throws Exception {
    // Given
    String phoneName = "Samsung Galaxy S9";
    // When
    RequestBuilder request =
        MockMvcRequestBuilders.get(BASE_API_URL + SLASH + phoneName)
            .contentType(MediaType.APPLICATION_JSON);

    String responseAsString =
        mockMvc
            .perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    var phone = objectMapper.readValue(responseAsString, Phone.class);

    Assertions.assertThat(phone)
        .isEqualTo(JsonUtils.readValueAsObject("/expected/phone.json", Phone.class));
  }

  @Test
  @Order(1)
  void shouldSuccessfullyBookPhone() throws Exception {
    // Given
    String phoneName = "Samsung Galaxy S9";
    String bookingRequest = "{\"username\": \"naveen\"}";
    // When
    RequestBuilder request =
        MockMvcRequestBuilders.post(BASE_API_URL + SLASH + phoneName + SLASH + "book")
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookingRequest)
            .accept(MediaType.APPLICATION_JSON);

    // When
    mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());

    // Then
    Assertions.assertThat(phoneRepository.findByName(phoneName))
        .isNotEmpty()
        .get()
        .extracting(PhoneEntity::isAvailable, PhoneEntity::getBookedBy, PhoneEntity::getBookedOn)
        .containsExactly(false, "naveen", LocalDate.now());
  }

  @Test
  @Order(2)
  void shouldSuccessfullyReturnPhone() throws Exception {
    // Given
    String phoneName = "Samsung Galaxy S9";
    // When
    RequestBuilder request =
        MockMvcRequestBuilders.post(BASE_API_URL + SLASH + phoneName + SLASH + "return")
            .accept(MediaType.APPLICATION_JSON);

    // When
    mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());

    // Then
    Assertions.assertThat(phoneRepository.findByName(phoneName))
        .isNotEmpty()
        .get()
        .extracting(PhoneEntity::isAvailable, PhoneEntity::getBookedBy, PhoneEntity::getBookedOn)
        .containsExactly(true, null, null);
  }
}
