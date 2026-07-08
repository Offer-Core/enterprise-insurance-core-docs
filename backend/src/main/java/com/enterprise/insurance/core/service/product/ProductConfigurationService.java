package com.enterprise.insurance.core.service.product;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enterprise.insurance.core.domain.product.ProductConfiguration;
import com.enterprise.insurance.core.event.ConfigurationChangeEvent;
import com.enterprise.insurance.core.exception.BusinessRuleException;
import com.enterprise.insurance.core.exception.EntityNotFoundException;
import com.enterprise.insurance.core.repository.product.ProductConfigurationRepository;
import com.enterprise.insurance.core.service.event.ConfigurationEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductConfigurationService {

    private final ProductConfigurationRepository productRepository;
    private final ConfigurationEventStore eventStore;

    @Transactional
    public ProductConfiguration createProduct(ProductConfiguration product, UUID userId) {
        if (productRepository.existsByProductCode(product.getProductCode())) {
            throw new BusinessRuleException(
                    "Product with code " + product.getProductCode() + " already exists");
        }
        product.setCreatedBy(userId);
        product.setUpdatedBy(userId);
        ProductConfiguration saved = productRepository.save(product);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.PRODUCT_CREATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.PRODUCT)
                .aggregateId(product.getProductCode()).version(1).newState(saved)
                .changedBy(userId.toString()).build());

        log.info("Product created: {} by user {}", product.getProductCode(), userId);
        return saved;
    }

    @Transactional
    public ProductConfiguration updateProduct(String productCode, ProductConfiguration updates,
            UUID userId) {
        ProductConfiguration existing = getProduct(productCode);
        Object oldState = cloneProduct(existing);

        existing.setProductNameAr(updates.getProductNameAr());
        existing.setProductNameEn(updates.getProductNameEn());
        existing.setProductType(updates.getProductType());
        existing.setLineOfBusiness(updates.getLineOfBusiness());
        existing.setProductDescriptionAr(updates.getProductDescriptionAr());
        existing.setProductDescriptionEn(updates.getProductDescriptionEn());
        existing.setMinimumPremium(updates.getMinimumPremium());
        existing.setMaximumPremium(updates.getMaximumPremium());
        existing.setConfig(updates.getConfig());
        existing.setEffectiveFrom(updates.getEffectiveFrom());
        existing.setEffectiveTo(updates.getEffectiveTo());
        existing.setUpdatedBy(userId);

        ProductConfiguration saved = productRepository.save(existing);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.PRODUCT_UPDATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.PRODUCT)
                .aggregateId(productCode).version(saved.getVersion()).oldState(oldState)
                .newState(saved).changedBy(userId.toString()).build());

        log.info("Product updated: {} by user {}", productCode, userId);
        return saved;
    }

    public ProductConfiguration getProduct(String productCode) {
        return productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new EntityNotFoundException("Product", productCode));
    }

    public List<ProductConfiguration> getActiveProducts() {
        return productRepository.findActiveProducts(LocalDate.now());
    }

    public List<ProductConfiguration> getProductsByLineOfBusiness(String lob) {
        return productRepository.findActiveByLineOfBusiness(lob, LocalDate.now());
    }

    public List<ProductConfiguration> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void deleteProduct(String productCode, UUID userId) {
        ProductConfiguration product = getProduct(productCode);
        product.setIsActive(false);
        product.setUpdatedBy(userId);
        productRepository.save(product);

        eventStore.append(ConfigurationChangeEvent.builder()
                .eventType(ConfigurationChangeEvent.EventTypes.PRODUCT_DEACTIVATED)
                .aggregateType(ConfigurationChangeEvent.AggregateTypes.PRODUCT)
                .aggregateId(productCode).version(product.getVersion()).oldState(product)
                .changedBy(userId.toString()).build());

        log.info("Product deactivated: {} by user {}", productCode, userId);
    }

    public void validateProduct(ProductConfiguration product) {
        if (product.getEffectiveFrom() == null) {
            throw new BusinessRuleException("Effective from date is required");
        }
        if (product.getEffectiveTo() != null
                && product.getEffectiveTo().isBefore(product.getEffectiveFrom())) {
            throw new BusinessRuleException("Effective to date must be after effective from date");
        }
        if (product.getMinimumPremium() != null && product.getMaximumPremium() != null
                && product.getMinimumPremium().compareTo(product.getMaximumPremium()) > 0) {
            throw new BusinessRuleException("Minimum premium cannot exceed maximum premium");
        }
    }

    private ProductConfiguration cloneProduct(ProductConfiguration original) {
        ProductConfiguration clone = new ProductConfiguration();
        clone.setProductCode(original.getProductCode());
        clone.setProductNameAr(original.getProductNameAr());
        clone.setProductNameEn(original.getProductNameEn());
        clone.setProductType(original.getProductType());
        clone.setLineOfBusiness(original.getLineOfBusiness());
        clone.setProductDescriptionAr(original.getProductDescriptionAr());
        clone.setProductDescriptionEn(original.getProductDescriptionEn());
        clone.setIsActive(original.getIsActive());
        clone.setMinimumPremium(original.getMinimumPremium());
        clone.setMaximumPremium(original.getMaximumPremium());
        clone.setConfig(original.getConfig());
        clone.setEffectiveFrom(original.getEffectiveFrom());
        clone.setEffectiveTo(original.getEffectiveTo());
        clone.setVersion(original.getVersion());
        return clone;
    }
}
