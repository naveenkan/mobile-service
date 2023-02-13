package com.mobile.controller;

import com.mobile.controller.Request.BookPhoneRequest;
import com.mobile.model.Phone;
import com.mobile.model.PhoneSpecification;
import com.mobile.service.PhoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/phones")
@RequiredArgsConstructor
public class PhoneController {
  private final PhoneService phoneService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Phone>> getAllPhones() {

    return ResponseEntity.ok(phoneService.getAllPhones());
  }

  @GetMapping(value = "/{name}/specs", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PhoneSpecification> getPhoneSpecification(@PathVariable String name) {

    return ResponseEntity.ok(phoneService.getPhoneSpecification(name));
  }

  @GetMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Phone> getPhone(@PathVariable String name) {

    return ResponseEntity.ok(phoneService.getPhone(name));
  }

  @PostMapping(
      value = "/{name}/book",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> bookPhone(
      @PathVariable String name, @RequestBody BookPhoneRequest request) {
    phoneService.bookPhone(name, request.getUsername());
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PostMapping(value = "/{name}/return", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> returnPhone(@PathVariable String name) {
    phoneService.returnPhone(name);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
