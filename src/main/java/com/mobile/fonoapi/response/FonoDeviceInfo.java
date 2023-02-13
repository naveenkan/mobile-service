package com.mobile.fonoapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mobile.model.PhoneSpecification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FonoDeviceInfo {
  private String deviceName;
  private String technology;
  private String _2G_bands;
  private String _3G_bands;
  private String _4G_bands;

  public PhoneSpecification toPhone() {
    return PhoneSpecification.builder()
        .deviceName(deviceName)
        .technology(technology)
        ._2G_bands(_2G_bands)
        ._3G_bands(_3G_bands)
        ._4G_bands(_4G_bands)
        .build();
  }
}
