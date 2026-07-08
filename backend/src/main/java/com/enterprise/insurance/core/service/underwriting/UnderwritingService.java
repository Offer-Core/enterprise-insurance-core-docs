package com.enterprise.insurance.core.service.underwriting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.product.ProductConfiguration;
import com.enterprise.insurance.core.domain.underwriting.UnderwritingRuleEntity;
import com.enterprise.insurance.core.domain.underwriting.UnderwritingRuleResult;
import com.enterprise.insurance.core.dto.underwriting.UnderwritingEvaluationRequest;
import com.enterprise.insurance.core.dto.underwriting.UnderwritingEvaluationResponse;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.product.ProductConfigurationRepository;
import com.enterprise.insurance.core.repository.product.UnderwritingRuleRepository;
import com.enterprise.insurance.core.service.event.ConfigurationEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnderwritingService {

    private final UnderwritingRuleRepository ruleRepository;
    private final ProductConfigurationRepository productRepository;
    private final ConfigurationEventStore eventStore;

    /**
     * Evaluate all active underwriting rules for a quote request.
     */
    public UnderwritingEvaluationResponse evaluateRules(UnderwritingEvaluationRequest request) {
        ProductConfiguration product =
                productRepository.findByProductCode(request.getProductCode()).orElseThrow(
                        () -> new EntityNotFoundException("Product", request.getProductCode()));

        List<UnderwritingRuleEntity> activeRules =
                ruleRepository.findByIsActiveTrueOrderByDisplayOrder();
        List<UnderwritingEvaluationResponse.RuleEvaluationResult> results = new ArrayList<>();

        for (UnderwritingRuleEntity rule : activeRules) {
            UnderwritingRuleResult result = evaluateSingleRule(rule, request);
            results.add(UnderwritingEvaluationResponse.RuleEvaluationResult.builder()
                    .ruleCode(result.getRuleCode()).ruleName(result.getRuleName())
                    .status(result.getStatus().name()).message(result.getMessage()).build());
        }

        // Determine overall status
        String overallStatus = determineOverallStatus(results);

        return UnderwritingEvaluationResponse.builder().overallStatus(overallStatus)
                .ruleResults(results).build();
    }

    /**
     * Evaluate a single underwriting rule against the quote request.
     */
    private UnderwritingRuleResult evaluateSingleRule(UnderwritingRuleEntity rule,
            UnderwritingEvaluationRequest request) {
        UnderwritingRuleResult.UnderwritingRuleStatus status =
                UnderwritingRuleResult.UnderwritingRuleStatus.PASS;
        String message = "Rule passed";

        boolean conditionMet = evaluateCondition(rule.getCondition(), request);

        if (conditionMet) {
            switch (rule.getAction()) {
                case REJECT:
                    status = UnderwritingRuleResult.UnderwritingRuleStatus.FAIL;
                    message = rule.getRejectionReason() != null ? rule.getRejectionReason()
                            : "Application rejected by rule: " + rule.getRuleCode();
                    break;
                case REFER:
                    status = UnderwritingRuleResult.UnderwritingRuleStatus.REFERRAL;
                    message = rule.getReferralReason() != null ? rule.getReferralReason()
                            : "Application referred by rule: " + rule.getRuleCode();
                    break;
                case SURCHARGE:
                    status = UnderwritingRuleResult.UnderwritingRuleStatus.PASS;
                    message = "Surcharge of " + rule.getSurchargeAmount() + " applied by rule: "
                            + rule.getRuleCode();
                    break;
                case DISCOUNT:
                    status = UnderwritingRuleResult.UnderwritingRuleStatus.PASS;
                    message = "Discount applied by rule: " + rule.getRuleCode();
                    break;
                case APPROVE:
                default:
                    status = UnderwritingRuleResult.UnderwritingRuleStatus.PASS;
                    message = "Rule passed: " + rule.getRuleCode();
                    break;
            }
        }

        return UnderwritingRuleResult.builder().ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleNameEn()).status(status).message(message).build();
    }

    /**
     * Evaluate a condition expression against the request data. Supports simple conditions like:
     * DRIVER_AGE < 21, VEHICLE_VALUE > 500000, etc.
     */
    private boolean evaluateCondition(String condition, UnderwritingEvaluationRequest request) {
        if (condition == null || condition.isBlank()) {
            return false;
        }

        String trimmed = condition.trim().toUpperCase();

        // Driver age conditions
        if (trimmed.contains("DRIVER_AGE")) {
            Integer driverAge = request.getDriverAge();
            if (driverAge == null)
                return false;
            return evaluateNumericCondition(trimmed, "DRIVER_AGE", driverAge);
        }

        // Vehicle value conditions
        if (trimmed.contains("VEHICLE_VALUE")) {
            Integer vehicleValue = request.getVehicleValue();
            if (vehicleValue == null)
                return false;
            return evaluateNumericCondition(trimmed, "VEHICLE_VALUE", vehicleValue);
        }

        // Violations conditions
        if (trimmed.contains("VIOLATIONS")) {
            Integer violations = request.getViolationsCount();
            if (violations == null)
                return false;
            return evaluateNumericCondition(trimmed, "VIOLATIONS", violations);
        }

        // Accidents conditions
        if (trimmed.contains("ACCIDENTS")) {
            Integer accidents = request.getAccidentsCount();
            if (accidents == null)
                return false;
            return evaluateNumericCondition(trimmed, "ACCIDENTS", accidents);
        }

        // Nationality conditions
        if (trimmed.contains("NATIONALITY")) {
            String nationality = request.getNationality();
            if (nationality == null)
                return false;
            if (trimmed.contains("NOT") || trimmed.contains("!=")) {
                return !trimmed.contains(nationality.toUpperCase());
            }
            return trimmed.contains(nationality.toUpperCase());
        }

        // Occupation conditions
        if (trimmed.contains("OCCUPATION")) {
            String occupation = request.getOccupation();
            if (occupation == null)
                return false;
            if (trimmed.contains("NOT") || trimmed.contains("!=")) {
                return !trimmed.contains(occupation.toUpperCase());
            }
            return trimmed.contains(occupation.toUpperCase());
        }

        // License validity
        if (trimmed.contains("LICENSE_VALID")) {
            return Boolean.TRUE.equals(request.getLicenseValid());
        }

        log.warn("Unrecognized condition: {}", condition);
        return false;
    }

    private boolean evaluateNumericCondition(String condition, String field, int value) {
        String[] parts = condition.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(field) && i + 1 < parts.length) {
                String operator = parts[i + 1];
                if (i + 2 < parts.length) {
                    try {
                        int threshold = Integer.parseInt(parts[i + 2]);
                        return switch (operator) {
                            case "<" -> value < threshold;
                            case ">" -> value > threshold;
                            case "<=" -> value <= threshold;
                            case ">=" -> value >= threshold;
                            case "==", "=" -> value == threshold;
                            case "!=" -> value != threshold;
                            default -> false;
                        };
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private String determineOverallStatus(
            List<UnderwritingEvaluationResponse.RuleEvaluationResult> results) {
        boolean hasFail = results.stream().anyMatch(r -> "FAIL".equals(r.getStatus()));
        boolean hasReferral = results.stream().anyMatch(r -> "REFERRAL".equals(r.getStatus()));

        if (hasFail)
            return "REJECTED";
        if (hasReferral)
            return "REFERRED";
        return "APPROVED";
    }

    // ========== Rule Management ==========

    public List<UnderwritingRuleEntity> getRules() {
        return ruleRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    public List<UnderwritingRuleEntity> getAllRules() {
        return ruleRepository.findAll();
    }

    public UnderwritingRuleEntity getRule(String ruleCode) {
        return ruleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new EntityNotFoundException("UnderwritingRule", ruleCode));
    }

    @Transactional
    public UnderwritingRuleEntity addRule(UnderwritingRuleEntity rule, UUID userId) {
        if (ruleRepository.existsByRuleCode(rule.getRuleCode())) {
            throw new BusinessRuleException(
                    "Rule with code " + rule.getRuleCode() + " already exists");
        }
        rule.setCreatedBy(userId);
        rule.setUpdatedBy(userId);
        UnderwritingRuleEntity saved = ruleRepository.save(rule);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.UNDERWRITING_RULE_CREATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.UNDERWRITING_RULE)
                .aggregateId(rule.getRuleCode()).newState(saved).changedBy(userId.toString())
                .build());

        log.info("Underwriting rule created: {} by user {}", rule.getRuleCode(), userId);
        return saved;
    }

    @Transactional
    public UnderwritingRuleEntity updateRule(String ruleCode, UnderwritingRuleEntity updates,
            UUID userId) {
        UnderwritingRuleEntity existing = getRule(ruleCode);
        existing.setRuleNameAr(updates.getRuleNameAr());
        existing.setRuleNameEn(updates.getRuleNameEn());
        existing.setRuleType(updates.getRuleType());
        existing.setSeverity(updates.getSeverity());
        existing.setCondition(updates.getCondition());
        existing.setAction(updates.getAction());
        existing.setSurchargeAmount(updates.getSurchargeAmount());
        existing.setReferralReason(updates.getReferralReason());
        existing.setRejectionReason(updates.getRejectionReason());
        existing.setDisplayOrder(updates.getDisplayOrder());
        existing.setUpdatedBy(userId);

        UnderwritingRuleEntity saved = ruleRepository.save(existing);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.UNDERWRITING_RULE_UPDATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.UNDERWRITING_RULE)
                .aggregateId(ruleCode).newState(saved).changedBy(userId.toString()).build());

        log.info("Underwriting rule updated: {} by user {}", ruleCode, userId);
        return saved;
    }

    @Transactional
    public void deleteRule(String ruleCode, UUID userId) {
        UnderwritingRuleEntity rule = getRule(ruleCode);
        rule.setIsActive(false);
        rule.setUpdatedBy(userId);
        ruleRepository.save(rule);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.UNDERWRITING_RULE_DELETED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.UNDERWRITING_RULE)
                .aggregateId(ruleCode).changedBy(userId.toString()).build());

        log.info("Underwriting rule deleted: {} by user {}", ruleCode, userId);
    }

    public void enableRule(String ruleCode, UUID userId) {
        UnderwritingRuleEntity rule = getRule(ruleCode);
        rule.setIsActive(true);
        rule.setUpdatedBy(userId);
        ruleRepository.save(rule);
        log.info("Underwriting rule enabled: {} by user {}", ruleCode, userId);
    }

    public void disableRule(String ruleCode, UUID userId) {
        UnderwritingRuleEntity rule = getRule(ruleCode);
        rule.setIsActive(false);
        rule.setUpdatedBy(userId);
        ruleRepository.save(rule);
        log.info("Underwriting rule disabled: {} by user {}", ruleCode, userId);
    }
}
