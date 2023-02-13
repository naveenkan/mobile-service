package com.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneSpecification {

  private String deviceName;
  private String technology;
  private String _2G_bands;
  private String _3G_bands;
  private String _4G_bands;
}
