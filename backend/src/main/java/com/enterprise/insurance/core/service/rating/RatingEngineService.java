package com.enterprise.insurance.core.service.rating;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.enterprise.insurance.core.domain.product.ProductConfiguration;
import com.enterprise.insurance.core.domain.rating.RatingFactorEntity;
import com.enterprise.insurance.core.domain.rating.RatingFactorResult;
import com.enterprise.insurance.core.domain.rating.RatingFactorValue;
import com.enterprise.insurance.core.dto.rating.PremiumCalculationRequest;
import com.enterprise.insurance.core.dto.rating.PremiumCalculationResponse;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.product.ProductConfigurationRepository;
import com.enterprise.insurance.core.repository.product.RatingFactorRepository;
import com.enterprise.insurance.core.repository.product.RatingFactorValueRepository;
import com.enterprise.insurance.core.service.event.ConfigurationEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingEngineService {

    private final RatingFactorRepository ratingFactorRepository;
    private final RatingFactorValueRepository ratingFactorValueRepository;
    private final ProductConfigurationRepository productRepository;
    private final ConfigurationEventStore eventStore;

    /**
     * Calculate premium for a quote request using configurable rating factors.
     */
    public PremiumCalculationResponse calculatePremium(PremiumCalculationRequest request) {
        ProductConfiguration product =
                productRepository.findByProductCode(request.getProductCode()).orElseThrow(
                        () -> new EntityNotFoundException("Product", request.getProductCode()));

        if (!product.isEffective()) {
            throw new BusinessRuleException(
                    "Product " + request.getProductCode() + " is not active or effective");
        }

        BigDecimal basePremium = request.getBasePremium();
        List<PremiumCalculationResponse.FactorApplication> factorApplications = new ArrayList<>();

        // Apply all active rating factors
        List<RatingFactorEntity> activeFactors =
                ratingFactorRepository.findByIsActiveTrueOrderByDisplayOrder();
        BigDecimal adjustedPremium = basePremium;

        for (RatingFactorEntity factor : activeFactors) {
            RatingFactorResult result = applyFactor(factor, request);
            if (result != null) {
                adjustedPremium = result.apply(adjustedPremium);
                factorApplications.add(PremiumCalculationResponse.FactorApplication.builder()
                        .factorCode(result.getFactorCode()).factorName(result.getFactorName())
                        .factorType(result.getFactorType().name()).value(result.getValue())
                        .description(result.getDescription()).build());
            }
        }

        // Apply tax
        BigDecimal taxRate = product.getConfig() != null && product.getConfig().getRating() != null
                ? product.getConfig().getRating().getTaxRate()
                : new BigDecimal("0.15");
        BigDecimal taxAmount = adjustedPremium.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalPremium = adjustedPremium.add(taxAmount);

        return PremiumCalculationResponse.builder().basePremium(basePremium)
                .totalPremium(adjustedPremium.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount).finalPremium(finalPremium.setScale(2, RoundingMode.HALF_UP))
                .currency(product.getConfig() != null && product.getConfig().getRating() != null
                        ? product.getConfig().getRating().getCurrency()
                        : "SAR")
                .factorApplications(factorApplications).build();
    }

    /**
     * Apply a single rating factor to the quote request.
     */
    private RatingFactorResult applyFactor(RatingFactorEntity factor,
            PremiumCalculationRequest request) {
        List<RatingFactorValue> values = ratingFactorValueRepository
                .findByFactorIdAndIsActiveTrueOrderByPriority(factor.getId());

        if (values.isEmpty()) {
            return null;
        }

        String inputValue = getFactorInputValue(factor.getFactorCode(), request);
        if (inputValue == null) {
            return null;
        }

        // Find matching value based on range or exact match
        RatingFactorValue matchedValue = findMatchingValue(values, inputValue);
        if (matchedValue == null) {
            return null;
        }

        return RatingFactorResult.builder().factorCode(factor.getFactorCode())
                .factorName(factor.getFactorNameEn()).factorType(factor.getFactorType())
                .value(matchedValue.getFactorValue()).description(matchedValue.getValueLabelEn())
                .build();
    }

    /**
     * Extract the input value for a factor from the request.
     */
    private String getFactorInputValue(String factorCode, PremiumCalculationRequest request) {
        // Check custom rating factors map first
        if (request.getRatingFactors() != null
                && request.getRatingFactors().containsKey(factorCode)) {
            return request.getRatingFactors().get(factorCode).toString();
        }

        return switch (factorCode.toUpperCase()) {
            case "NCD" -> request.getClaimFreeYears() != null
                    ? request.getClaimFreeYears().toString()
                    : null;
            case "DRIVER_AGE" -> request.getDriverAge() != null ? request.getDriverAge().toString()
                    : null;
            case "VEHICLE_VALUE" -> request.getVehicleValue() != null
                    ? request.getVehicleValue().toString()
                    : null;
            case "VEHICLE_USE" -> request.getVehicleUseType();
            case "VEHICLE_MODEL_YEAR" -> request.getVehicleModelYear() != null
                    ? request.getVehicleModelYear().toString()
                    : null;
            case "PARKING_LOCATION" -> request.getParkingLocation();
            case "ANNUAL_MILEAGE" -> request.getAnnualMileage() != null
                    ? request.getAnnualMileage().toString()
                    : null;
            case "VIOLATIONS" -> request.getViolationsCount() != null
                    ? request.getViolationsCount().toString()
                    : null;
            case "ACCIDENTS" -> request.getAccidentsCount() != null
                    ? request.getAccidentsCount().toString()
                    : null;
            default -> null;
        };
    }

    /**
     * Find the matching factor value based on range or exact match.
     */
    private RatingFactorValue findMatchingValue(List<RatingFactorValue> values, String input) {
        // Try exact match first
        Optional<RatingFactorValue> exactMatch = values.stream().filter(v -> v.getValueCode()
                .equalsIgnoreCase(input)
                || (v.getValueLabelEn() != null && v.getValueLabelEn().equalsIgnoreCase(input)))
                .findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }

        // Try range match (for numeric values)
        try {
            BigDecimal numericInput = new BigDecimal(input);
            return values.stream().filter(v -> {
                boolean minMatch = v.getMinValue() == null
                        || new BigDecimal(v.getMinValue()).compareTo(numericInput) <= 0;
                boolean maxMatch = v.getMaxValue() == null
                        || new BigDecimal(v.getMaxValue()).compareTo(numericInput) >= 0;
                return minMatch && maxMatch;
            }).findFirst().orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ========== Rating Factor Management ==========

    public List<RatingFactorEntity> getRatingFactors() {
        return ratingFactorRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    public List<RatingFactorEntity> getAllRatingFactors() {
        return ratingFactorRepository.findAll();
    }

    public RatingFactorEntity getRatingFactor(String factorCode) {
        return ratingFactorRepository.findByFactorCode(factorCode)
                .orElseThrow(() -> new EntityNotFoundException("RatingFactor", factorCode));
    }

    public RatingFactorEntity updateRatingFactor(String factorCode, RatingFactorEntity updates,
            UUID userId) {
        RatingFactorEntity existing = getRatingFactor(factorCode);
        existing.setFactorNameAr(updates.getFactorNameAr());
        existing.setFactorNameEn(updates.getFactorNameEn());
        existing.setFactorType(updates.getFactorType());
        existing.setCalculationMethod(updates.getCalculationMethod());
        existing.setParameters(updates.getParameters());
        existing.setDisplayOrder(updates.getDisplayOrder());
        existing.setUpdatedBy(userId);

        RatingFactorEntity saved = ratingFactorRepository.save(existing);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.RATING_FACTOR_UPDATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.RATING_FACTOR)
                .aggregateId(factorCode).newState(saved).changedBy(userId.toString()).build());

        log.info("Rating factor updated: {} by user {}", factorCode, userId);
        return saved;
    }

    public void enableFactor(String factorCode, UUID userId) {
        RatingFactorEntity factor = getRatingFactor(factorCode);
        factor.setIsActive(true);
        factor.setUpdatedBy(userId);
        ratingFactorRepository.save(factor);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.RATING_FACTOR_ENABLED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.RATING_FACTOR)
                .aggregateId(factorCode).changedBy(userId.toString()).build());

        log.info("Rating factor enabled: {} by user {}", factorCode, userId);
    }

    public void disableFactor(String factorCode, UUID userId) {
        RatingFactorEntity factor = getRatingFactor(factorCode);
        factor.setIsActive(false);
        factor.setUpdatedBy(userId);
        ratingFactorRepository.save(factor);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.RATING_FACTOR_DISABLED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.RATING_FACTOR)
                .aggregateId(factorCode).changedBy(userId.toString()).build());

        log.info("Rating factor disabled: {} by user {}", factorCode, userId);
    }

    public List<RatingFactorValue> getFactorValues(UUID factorId) {
        return ratingFactorValueRepository.findByFactorIdAndIsActiveTrueOrderByPriority(factorId);
    }

    public RatingFactorValue addFactorValue(RatingFactorValue value) {
        return ratingFactorValueRepository.save(value);
    }
}
