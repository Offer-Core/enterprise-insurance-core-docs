package com.enterprise.insurance.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(name = "building_number", length = 20)
    private String buildingNumber;

    @Column(name = "street_name_ar", length = 200)
    private String streetNameAr;

    @Column(name = "street_name_en", length = 200)
    private String streetNameEn;

    @Column(name = "district_ar", length = 200)
    private String districtAr;

    @Column(name = "district_en", length = 200)
    private String districtEn;

    @Column(name = "city_code", length = 20)
    private String cityCode;

    @Column(name = "city_ar", length = 100)
    private String cityAr;

    @Column(name = "city_en", length = 100)
    private String cityEn;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "additional_number", length = 10)
    private String additionalNumber;

    @Column(name = "unit_number", length = 10)
    private String unitNumber;

    @Column(name = "country_code", length = 3)
    @Builder.Default
    private String countryCode = "SA";
}
