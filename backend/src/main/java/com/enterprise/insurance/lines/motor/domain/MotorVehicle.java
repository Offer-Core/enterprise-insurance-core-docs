package com.enterprise.insurance.lines.motor.domain;

import com.enterprise.insurance.core.domain.Policy;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "motor_vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotorVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "plate_number", length = 10, nullable = false)
    private String plateNumber;

    @Column(name = "plate_letter_code", length = 2)
    private String plateLetterCode;

    @Column(name = "plate_type", length = 20)
    private String plateType; // PRIVATE, PUBLIC, RENTAL

    @Column(name = "vehicle_make", length = 50, nullable = false)
    private String vehicleMake;

    @Column(name = "vehicle_model", length = 50, nullable = false)
    private String vehicleModel;

    @Column(name = "vehicle_year", nullable = false)
    private Integer vehicleYear;

    @Column(name = "vehicle_body_type", length = 30)
    private String vehicleBodyType; // SEDAN, SUV, TRUCK

    @Column(name = "chassis_number", length = 30, unique = true)
    private String chassisNumber;

    @Column(name = "engine_number", length = 30)
    private String engineNumber;

    @Column(length = 20)
    private String color;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
