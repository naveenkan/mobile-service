package com.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Phone {

  private Long id;
  private String name;
  private boolean isAvailable;

  private String bookedBy;

  private LocalDate bookedOn;
}
