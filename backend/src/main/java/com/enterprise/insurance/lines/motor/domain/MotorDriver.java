package com.enterprise.insurance.lines.motor.domain;

import com.enterprise.insurance.core.domain.Customer;
import com.enterprise.insurance.core.domain.Policy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "motor_drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotorDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "is_primary_driver")
    private Boolean isPrimaryDriver = false;

    @Column(name = "driving_license_number", length = 20, nullable = false)
    private String drivingLicenseNumber;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "license_country", length = 3)
    private String licenseCountry = "SA";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> claimHistory;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
