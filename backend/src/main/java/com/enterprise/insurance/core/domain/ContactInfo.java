package com.enterprise.insurance.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {

    @Column(length = 100)
    @Email
    private String email;

    @Column(name = "mobile_number", length = 20)
    @Pattern(regexp = "^05[0-9]{8}$", message = "Mobile must be a valid Saudi number")
    private String mobileNumber;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "ar";

    @Column(name = "preferred_contact_method", length = 20)
    @Builder.Default
    private String preferredContactMethod = "SMS";
}
