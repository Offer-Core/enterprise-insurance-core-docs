package com.enterprise.insurance.core.service.product;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.product.BenefitCatalog;
import com.enterprise.insurance.core.domain.product.ProductConfiguration;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.product.BenefitCatalogRepository;
import com.enterprise.insurance.core.service.event.ConfigurationEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogService {

    private final ProductConfigurationService productService;
    private final ProductBenefitService benefitService;
    private final BenefitCatalogRepository benefitCatalogRepository;
    private final ConfigurationEventStore eventStore;

    /**
     * Get full product catalog with benefits for each product.
     */
    public Map<String, Object> getProductCatalog() {
        List<ProductConfiguration> products = productService.getActiveProducts();
        Map<String, Object> catalog = new LinkedHashMap<>();
        catalog.put("totalProducts", products.size());
        catalog.put("products",
                products.stream().map(this::enrichWithBenefits).collect(Collectors.toList()));
        return catalog;
    }

    /**
     * Get detailed product information including benefits.
     */
    public Map<String, Object> getProductDetails(String productCode) {
        ProductConfiguration product = productService.getProduct(productCode);
        return enrichWithBenefits(product);
    }

    /**
     * Compare multiple products side by side.
     */
    public List<Map<String, Object>> getProductComparison(List<String> productCodes) {
        return productCodes.stream().map(code -> {
            try {
                return getProductDetails(code);
            } catch (EntityNotFoundException e) {
                log.warn("Product not found for comparison: {}", code);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ========== Benefit Catalog Management ==========

    @Transactional
    public BenefitCatalog createBenefitCatalog(BenefitCatalog benefit, UUID userId) {
        if (benefitCatalogRepository.existsByBenefitCode(benefit.getBenefitCode())) {
            throw new BusinessRuleException("Benefit catalog entry with code "
                    + benefit.getBenefitCode() + " already exists");
        }
        benefit.setCreatedBy(userId);
        benefit.setUpdatedBy(userId);
        BenefitCatalog saved = benefitCatalogRepository.save(benefit);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.BENEFIT_CATALOG_CREATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.BENEFIT_CATALOG)
                .aggregateId(benefit.getBenefitCode()).newState(saved).changedBy(userId.toString())
                .build());

        log.info("Benefit catalog entry created: {} by user {}", benefit.getBenefitCode(), userId);
        return saved;
    }

    @Transactional
    public BenefitCatalog updateBenefitCatalog(String benefitCode, BenefitCatalog updates,
            UUID userId) {
        BenefitCatalog existing = benefitCatalogRepository.findByBenefitCode(benefitCode)
                .orElseThrow(() -> new EntityNotFoundException("BenefitCatalog", benefitCode));

        existing.setBenefitNameAr(updates.getBenefitNameAr());
        existing.setBenefitNameEn(updates.getBenefitNameEn());
        existing.setBenefitDescriptionAr(updates.getBenefitDescriptionAr());
        existing.setBenefitDescriptionEn(updates.getBenefitDescriptionEn());
        existing.setBenefitCategory(updates.getBenefitCategory());
        existing.setDefaultPrice(updates.getDefaultPrice());
        existing.setMaximumPrice(updates.getMaximumPrice());
        existing.setCalculationMethod(updates.getCalculationMethod());
        existing.setCalculationParams(updates.getCalculationParams());
        existing.setIsActive(updates.getIsActive());
        existing.setApplicableProductTypes(updates.getApplicableProductTypes());
        existing.setUpdatedBy(userId);

        BenefitCatalog saved = benefitCatalogRepository.save(existing);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.BENEFIT_CATALOG_UPDATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.BENEFIT_CATALOG)
                .aggregateId(benefitCode).newState(saved).changedBy(userId.toString()).build());

        log.info("Benefit catalog entry updated: {} by user {}", benefitCode, userId);
        return saved;
    }

    public List<BenefitCatalog> getActiveBenefits() {
        return benefitCatalogRepository.findByIsActiveTrue();
    }

    public BenefitCatalog getBenefitByCode(String benefitCode) {
        return benefitCatalogRepository.findByBenefitCode(benefitCode)
                .orElseThrow(() -> new EntityNotFoundException("BenefitCatalog", benefitCode));
    }

    // ========== Private Helpers ==========

    private Map<String, Object> enrichWithBenefits(ProductConfiguration product) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("product", product);
        details.put("benefits", benefitService.getBenefitsByProduct(product.getId()));
        details.put("mandatoryBenefits", benefitService.getMandatoryBenefits(product.getId()));
        details.put("defaultBenefits", benefitService.getDefaultBenefits(product.getId()));
        return details;
    }
}
