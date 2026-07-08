package com.enterprise.insurance.core.configuration;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing the product catalog. Products are configuration-driven and can be
 * modified without code changes.
 */
public interface ProductCatalog {

    Optional<ProductConfig> getProduct(String productCode);

    List<ProductConfig> getActiveProducts();

    List<ProductConfig> getProductsByLineOfBusiness(String lineOfBusiness);

    ProductConfig createProduct(ProductConfig product);

    ProductConfig updateProduct(String productCode, ProductConfig product);

    void deactivateProduct(String productCode);

    boolean isProductActive(String productCode);
}
