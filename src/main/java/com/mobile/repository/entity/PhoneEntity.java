package com.mobile.repository.entity;

import com.mobile.model.Phone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "PHONE")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PHONE_SEQUENCE")
  @SequenceGenerator(name = "PHONE_SEQUENCE", sequenceName = "PHONE_SEQUENCE", allocationSize = 1)
  private Long id;

  private String name;

  private boolean available;

  @Column(name = "booked_on")
  private LocalDate bookedOn;

  @Column(name = "booked_by")
  private String bookedBy;

  public static PhoneEntity fromModel(Phone phone) {
    return PhoneEntity.builder()
        .id(phone.getId())
        .name(phone.getName())
        .available(phone.isAvailable())
        .bookedOn(phone.getBookedOn())
        .bookedBy(phone.getBookedBy())
        .build();
  }

  public Phone toModel() {
    return Phone.builder()
        .id(id)
        .name(name)
        .isAvailable(available)
        .bookedBy(bookedBy)
        .bookedOn(bookedOn)
        .build();
  }
}
