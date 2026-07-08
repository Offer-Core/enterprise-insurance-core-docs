package com.enterprise.insurance.core.domain;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "customers", schema = "core",
        indexes = {@Index(name = "idx_customers_number", columnList = "customer_number"),
                @Index(name = "idx_customers_tenant", columnList = "tenant_id"),
                @Index(name = "idx_customers_national_id", columnList = "national_id_encrypted")})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "party_id")
    private UUID partyId;

    @Column(name = "customer_number", length = 20, unique = true, nullable = false)
    private String customerNumber;

    // Identity (encrypted at application level - AES-256-GCM)
    @Column(name = "national_id_encrypted", nullable = false)
    private String nationalIdEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", length = 20, nullable = false)
    private IdentityType identityType;

    // Bilingual names
    @Column(name = "full_name_ar", length = 200, nullable = false)
    private String fullNameAr;

    @Column(name = "full_name_en", length = 200)
    private String fullNameEn;

    @Column(name = "first_name_ar", length = 100)
    private String firstNameAr;

    @Column(name = "middle_name_ar", length = 100)
    private String middleNameAr;

    @Column(name = "last_name_ar", length = 100)
    private String lastNameAr;

    @Column(name = "first_name_en", length = 100)
    private String firstNameEn;

    @Column(name = "middle_name_en", length = 100)
    private String middleNameEn;

    @Column(name = "last_name_en", length = 100)
    private String lastNameEn;

    // Contact
    @Embedded
    private ContactInfo contactInfo;

    // Saudi-specific fields
    @Column(name = "nationality_code", length = 3)
    private String nationalityCode;

    @Column(name = "date_of_birth_hijri", length = 15)
    private String dateOfBirthHijri;

    @Column(name = "date_of_birth_gregorian")
    private LocalDate dateOfBirthGregorian;

    @Column(length = 1)
    private String gender;

    @Column(name = "occupation_code", length = 20)
    private String occupationCode;

    @Column(name = "occupation_ar", length = 200)
    private String occupationAr;

    @Column(name = "occupation_en", length = 200)
    private String occupationEn;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    // Address
    @Embedded
    private Address address;

    // Dynamic extensions
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> dynamicAttributes = Map.of();

    @Column(name = "data_classification", length = 20)
    @Builder.Default
    private String dataClassification = "CONFIDENTIAL";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "customer_type", length = 20)
    private String customerType;

    @Column(name = "kyc_status", length = 20)
    @Builder.Default
    private String kycStatus = "PENDING";

    @Column(name = "kyc_verified_at")
    private LocalDate kycVerifiedAt;
}
