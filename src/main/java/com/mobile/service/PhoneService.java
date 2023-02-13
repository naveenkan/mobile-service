package com.mobile.service;

import com.mobile.exception.PhoneNotAvailableException;
import com.mobile.exception.PhoneNotCheckedOutException;
import com.mobile.exception.PhoneNotFoundException;
import com.mobile.fonoapi.FonoApiClient;
import com.mobile.fonoapi.response.FonoDeviceInfo;
import com.mobile.model.Phone;
import com.mobile.model.PhoneSpecification;
import com.mobile.repository.PhoneRepository;
import com.mobile.repository.entity.PhoneEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneService {
  private final PhoneRepository phoneRepository;
  private final FonoApiClient fonoApiClient;

  public PhoneSpecification getPhoneSpecification(String name) {
    return fonoApiClient.getPhoneSpecification(name).stream()
        .findFirst()
        .map(FonoDeviceInfo::toPhone)
        .orElseThrow(() -> new PhoneNotFoundException(name + "phone Not Available"));
  }

  public void bookPhone(String name, String username) {
    var phone = getPhone(name);

    if (!phone.isAvailable()) {
      throw new PhoneNotAvailableException(name + " not available for booking");
    }

    phone.setAvailable(false);
    phone.setBookedOn(LocalDate.now());
    phone.setBookedBy(username);

    phoneRepository.save(PhoneEntity.fromModel(phone));
  }

  public void returnPhone(String name) {
    var phone = getPhone(name);

    if (phone.isAvailable()) {
      throw new PhoneNotCheckedOutException(name + " not yet checked out");
    }

    phone.setAvailable(true);
    phone.setBookedOn(null);
    phone.setBookedBy(null);

    phoneRepository.save(PhoneEntity.fromModel(phone));
  }

  public Phone getPhone(String name) {
    return phoneRepository
        .findByName(name)
        .map(PhoneEntity::toModel)
        .orElseThrow(
            () -> new PhoneNotFoundException("No Mobile Phone exists in system with name" + name));
  }

  public List<Phone> getAllPhones() {
    return phoneRepository.findAll().stream().map(PhoneEntity::toModel).toList();
  }
}
