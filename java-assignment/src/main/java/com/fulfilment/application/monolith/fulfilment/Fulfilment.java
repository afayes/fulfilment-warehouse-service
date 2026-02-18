package com.fulfilment.application.monolith.fulfilment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfilment",
    uniqueConstraints =
        @UniqueConstraint(
            columnNames = {"storeId", "productId", "warehouseBusinessUnitCode"}))
public class Fulfilment {

  @Id @GeneratedValue public Long id;

  @Column(nullable = false)
  public Long storeId;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;
}
