package com.enterprise.insurance.core.service.product;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.product.ProductBenefit;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.product.ProductBenefitRepository;
import com.enterprise.insurance.core.service.event.ConfigurationEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductBenefitService {

    private final ProductBenefitRepository benefitRepository;
    private final ConfigurationEventStore eventStore;

    @Transactional
    public ProductBenefit addBenefit(ProductBenefit benefit, UUID userId) {
        benefit.setCreatedBy(userId);
        benefit.setUpdatedBy(userId);
        ProductBenefit saved = benefitRepository.save(benefit);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.BENEFIT_ADDED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.BENEFIT)
                .aggregateId(benefit.getBenefitCode()).newState(saved).changedBy(userId.toString())
                .build());

        log.info("Benefit {} added to product {} by user {}", benefit.getBenefitCode(),
                benefit.getProductId(), userId);
        return saved;
    }

    @Transactional
    public ProductBenefit updateBenefit(UUID benefitId, ProductBenefit updates, UUID userId) {
        ProductBenefit existing = benefitRepository.findById(benefitId).orElseThrow(
                () -> new EntityNotFoundException("ProductBenefit", benefitId.toString()));

        existing.setBenefitNameAr(updates.getBenefitNameAr());
        existing.setBenefitNameEn(updates.getBenefitNameEn());
        existing.setBenefitDescriptionAr(updates.getBenefitDescriptionAr());
        existing.setBenefitDescriptionEn(updates.getBenefitDescriptionEn());
        existing.setBenefitType(updates.getBenefitType());
        existing.setDefaultPrice(updates.getDefaultPrice());
        existing.setPriceOverride(updates.getPriceOverride());
        existing.setIsOptional(updates.getIsOptional());
        existing.setIsIncludedByDefault(updates.getIsIncludedByDefault());
        existing.setIsActive(updates.getIsActive());
        existing.setConditions(updates.getConditions());
        existing.setDisplayOrder(updates.getDisplayOrder());
        existing.setUpdatedBy(userId);

        ProductBenefit saved = benefitRepository.save(existing);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.BENEFIT_UPDATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.BENEFIT)
                .aggregateId(existing.getBenefitCode()).newState(saved).changedBy(userId.toString())
                .build());

        log.info("Benefit {} updated by user {}", existing.getBenefitCode(), userId);
        return saved;
    }

    @Transactional
    public void removeBenefit(UUID benefitId, UUID userId) {
        ProductBenefit benefit = benefitRepository.findById(benefitId).orElseThrow(
                () -> new EntityNotFoundException("ProductBenefit", benefitId.toString()));

        benefit.setIsActive(false);
        benefit.setUpdatedBy(userId);
        benefitRepository.save(benefit);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.BENEFIT_REMOVED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.BENEFIT)
                .aggregateId(benefit.getBenefitCode()).newState(benefit)
                .changedBy(userId.toString()).build());

        log.info("Benefit {} removed from product {} by user {}", benefit.getBenefitCode(),
                benefit.getProductId(), userId);
    }

    public List<ProductBenefit> getBenefitsByProduct(UUID productId) {
        return benefitRepository.findByProductIdAndIsActiveTrueOrderByDisplayOrder(productId);
    }

    public List<ProductBenefit> getDefaultBenefits(UUID productId) {
        return benefitRepository.findByProductIdAndIsIncludedByDefaultTrue(productId);
    }

    public List<ProductBenefit> getMandatoryBenefits(UUID productId) {
        return benefitRepository.findByProductIdAndIsOptionalFalse(productId);
    }

    public ProductBenefit getBenefit(UUID productId, String benefitCode) {
        return benefitRepository.findByProductIdAndBenefitCode(productId, benefitCode)
                .orElseThrow(() -> new EntityNotFoundException("ProductBenefit",
                        "productId=" + productId + ", benefitCode=" + benefitCode));
    }
}
