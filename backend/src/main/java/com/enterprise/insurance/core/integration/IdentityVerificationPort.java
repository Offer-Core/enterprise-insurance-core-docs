package com.enterprise.insurance.core.integration;

import java.time.LocalDate;

/**
 * Port interface for identity verification via Yakeen (Saudi national system).
 */
public interface IdentityVerificationPort {

    /**
     * Verify a Saudi citizen's identity.
     *
     * @param nationalId 10-digit Saudi National ID (NIN)
     * @param dateOfBirth Date of birth in Gregorian calendar
     * @return verification result
     */
    VerificationResult verifyCitizen(String nationalId, LocalDate dateOfBirth);

    /**
     * Verify a resident's identity via Iqama.
     *
     * @param iqamaNumber 10-digit Iqama number
     * @param expiryDate Iqama expiry date
     * @return verification result
     */
    VerificationResult verifyResident(String iqamaNumber, LocalDate expiryDate);

    /**
     * Verify a company's identity via CR number.
     *
     * @param commercialRegistration Commercial Registration number
     * @return verification result
     */
    VerificationResult verifyCompany(String commercialRegistration);

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class VerificationResult {
        private boolean verified;
        private String fullNameAr;
        private String fullNameEn;
        private LocalDate dateOfBirth;
        private String dateOfBirthHijri;
        private String gender;
        private String nationalityCode;
        private String idExpiryDate;
        private boolean isAlive;
        private String address;
        private String phoneNumber;
        private String errorCode;
        private String errorMessage;
        private String yakeenReference;
    }
}
