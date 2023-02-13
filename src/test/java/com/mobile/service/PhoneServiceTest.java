package com.mobile.service;

import com.mobile.exception.PhoneNotAvailableException;
import com.mobile.exception.PhoneNotCheckedOutException;
import com.mobile.fonoapi.FonoApiClient;
import com.mobile.fonoapi.response.FonoDeviceInfo;
import com.mobile.model.PhoneSpecification;
import com.mobile.repository.PhoneRepository;
import com.mobile.repository.entity.PhoneEntity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PhoneServiceTest {
  /*
  class under test
  */
  PhoneService phoneService;

  @Mock private PhoneRepository phoneRepository;
  @Mock private FonoApiClient fonoApiClient;

  @BeforeEach
  public void setUp() {
    phoneService = new PhoneService(phoneRepository, fonoApiClient);
  }

  /**
   * Test that {@link PhoneService#getPhoneSpecification(String)} able to get phone specifications
   * from fono api
   */
  @Test
  void shouldGetPhoneSpecification() {
    // Given
    var deviceName = "Samsung Galaxy S9";
    var deviceInfos =
        List.of(
            FonoDeviceInfo.builder()
                .deviceName(deviceName)
                .technology("GSM")
                ._2G_bands("GSM 1800 / 1900 / 850 / 900 MHz")
                ._3G_bands("UMTS 1900 / 2100 / 850 / 900 MHz")
                ._4G_bands("TD-LTE 2600(band 38) / 2300(band 40) /")
                .build());
    when(fonoApiClient.getPhoneSpecification(deviceName)).thenReturn(deviceInfos);
    // When
    PhoneSpecification phoneSpecification = phoneService.getPhoneSpecification(deviceName);
    // Then
    assertThat(phoneSpecification)
        .extracting(PhoneSpecification::getTechnology, PhoneSpecification::get_4G_bands)
        .containsExactly("GSM", "TD-LTE 2600(band 38) / 2300(band 40) /");
  }

  /**
   * Tests that { {@link PhoneService#bookPhone(String, String)} should save phone booking details
   */
  @Test
  void bookPhoneTest() {
    // Given
    var phoneName = "samsung galaxy s9";
    var userName = "naveen";
    var phoneEntityCaptor = ArgumentCaptor.forClass(PhoneEntity.class);
    when(phoneRepository.findByName(phoneName))
        .thenReturn(
            Optional.ofNullable(PhoneEntity.builder().name(phoneName).available(true).build()));

    // When
    phoneService.bookPhone(phoneName, userName);

    // Then
    verify(phoneRepository).save(phoneEntityCaptor.capture());
    var phoneEntity = phoneEntityCaptor.getValue();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(phoneEntity.isAvailable()).isFalse();
    softly.assertThat(phoneEntity.getBookedOn()).isEqualTo(LocalDate.now());
    softly.assertThat(phoneEntity.getBookedBy()).isEqualTo(userName);
    softly.assertThat(phoneEntity.getName()).isEqualTo(phoneName);
    softly.assertAll();
  }

  /**
   * Tests that { {@link PhoneService#bookPhone(String, String)} should throw
   * PhoneNotAvailableException when phone is not available
   */
  @Test
  void shouldThrowPhoneNotAvailableException() {
    // Given
    var phoneName = "samsung galaxy s9";
    var userName = "naveen";
    when(phoneRepository.findByName(phoneName))
        .thenReturn(Optional.ofNullable(PhoneEntity.builder().available(false).build()));

    // When && Then
    assertThatThrownBy(() -> phoneService.bookPhone(phoneName, userName))
        .isInstanceOf(PhoneNotAvailableException.class)
        .hasMessageContaining(phoneName + " not available for booking");

    verify(phoneRepository, times(0)).save(any(PhoneEntity.class));
  }

  /** Test {@link PhoneService#returnPhone(String)} */
  @Test
  void returnPhoneTest() {
    // Given
    var phoneName = "samsung galaxy s9";
    var userName = "naveen";
    var phoneEntityCaptor = ArgumentCaptor.forClass(PhoneEntity.class);
    when(phoneRepository.findByName(phoneName))
        .thenReturn(
            Optional.ofNullable(
                PhoneEntity.builder()
                    .name(phoneName)
                    .available(false)
                    .bookedOn(LocalDate.now())
                    .bookedBy(userName)
                    .build()));

    // When
    phoneService.returnPhone(phoneName);

    // Then
    verify(phoneRepository).save(phoneEntityCaptor.capture());
    var phoneEntity = phoneEntityCaptor.getValue();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(phoneEntity.isAvailable()).isTrue();
    softly.assertThat(phoneEntity.getBookedOn()).isNull();
    softly.assertThat(phoneEntity.getBookedBy()).isNull();
    softly.assertThat(phoneEntity.getName()).isEqualTo(phoneName);
    softly.assertAll();
  }

  /** Test that {@link PhoneService#returnPhone(String)} throw exception when phone is available */
  @Test
  void shouldThrowPhoneNotCheckedOutException() {
    // Given
    var phoneName = "samsung galaxy s9";
    when(phoneRepository.findByName(phoneName))
        .thenReturn(Optional.ofNullable(PhoneEntity.builder().available(true).build()));

    // When && Then
    assertThatThrownBy(() -> phoneService.returnPhone(phoneName))
        .isInstanceOf(PhoneNotCheckedOutException.class)
        .hasMessageContaining(phoneName + " not yet checked out");

    verify(phoneRepository, times(0)).save(any(PhoneEntity.class));
  }
}
