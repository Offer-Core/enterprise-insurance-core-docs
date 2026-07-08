package com.enterprise.insurance.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.enterprise.insurance.core.domain.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByNationalIdEncrypted(String nationalIdEncrypted);

    Optional<Customer> findByCustomerNumber(String customerNumber);

    @Query("SELECT c FROM Customer c WHERE c.contactInfo.mobileNumber = :mobile")
    Optional<Customer> findByMobileNumber(@Param("mobile") String mobile);

    @Query("SELECT c FROM Customer c WHERE c.contactInfo.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    List<Customer> findByFullNameArContainingIgnoreCase(String fullNameAr);

    List<Customer> findByFullNameEnContainingIgnoreCase(String fullNameEn);

    List<Customer> findByTenantId(String tenantId);

    List<Customer> findByIsActiveTrue();

    long countByTenantId(String tenantId);

    @Query("SELECT c FROM Customer c WHERE c.kycStatus = :status")
    List<Customer> findByKycStatus(@Param("status") String status);
}
