package com.inventory.service;

import com.inventory.dto.request.ProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.exception.ValidationException;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.repository.*;
import com.inventory.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final String DEFAULT_UNIT_OF_MEASURE = "шт";

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final SecurityUtils      securityUtils;

    public Page<ProductResponse> findAll(Pageable pageable) {
        Long companyId = securityUtils.getCurrentCompanyId();
        log.debug("Отримання списку активних товарів для компанії {}, сторінка: {}",
            companyId, pageable.getPageNumber());
        return productRepository.findByIsActiveTrueAndCompanyId(companyId, pageable)
            .map(this::toResponse);
    }

    public ProductResponse findById(Long id) {
        return toResponse(getProductEntityById(id));
    }

    public ProductResponse findBySku(String sku) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return toResponse(productRepository.findBySkuAndCompanyId(sku, companyId)
            .orElseThrow(() -> new ResourceNotFoundException("Товар з SKU '" + sku + "' не знайдено")));
    }

    public List<ProductResponse> search(String name) {
        Long companyId = securityUtils.getCurrentCompanyId();
        log.debug("Пошук товарів за назвою: {} у компанії {}", name, companyId);
        return productRepository.searchByNameAndCompanyId(name, companyId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Long companyId = securityUtils.getCurrentCompanyId();
        log.info("Спроба створення нового товару з SKU: {} для компанії {}", request.getSku(), companyId);

        if (productRepository.findBySkuAndCompanyId(request.getSku(), companyId).isPresent()) {
            log.warn("Помилка створення: товар з SKU '{}' вже існує", request.getSku());
            throw new ValidationException("Товар з SKU '" + request.getSku() + "' вже існує");
        }

        Product product = fromRequest(request, companyId);
        Product savedProduct = productRepository.save(product);

        log.info("Товар успішно створено з ID: {}", savedProduct.getId());
        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Оновлення даних товару з ID: {}", id);
        Product product = getProductEntityById(id);

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitOfMeasure(request.getUnitOfMeasure());

        if (request.getOrderingCost() != null)    product.setOrderingCost(request.getOrderingCost());
        if (request.getHoldingCostRate() != null) product.setHoldingCostRate(request.getHoldingCostRate());
        if (request.getServiceLevel() != null)    product.setServiceLevel(request.getServiceLevel());

        product.setCategory(getCategoryById(request.getCategoryId()));
        product.setSupplier(getSupplierById(request.getSupplierId()));

        Product updatedProduct = productRepository.save(product);
        log.info("Товар з ID: {} успішно оновлено", id);

        return toResponse(updatedProduct);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Запит на м'яке видалення товару з ID: {}", id);
        Product product = getProductEntityById(id);
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Товар з ID: {} успішно деактивовано", id);
    }

    // ── Допоміжні методи ────────────────────────────────────────

    public Product getProductEntityById(Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return productRepository.findById(id)
            .filter(p -> p.getCompanyId().equals(companyId))
            .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));
    }

    private Category getCategoryById(Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return categoryRepository.findById(id)
            .filter(c -> c.getCompanyId().equals(companyId))
            .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));
    }

    private Supplier getSupplierById(Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return supplierRepository.findById(id)
            .filter(s -> s.getCompanyId().equals(companyId))
            .orElseThrow(() -> new ResourceNotFoundException("Постачальника з ID " + id + " не знайдено"));
    }

    // ── Маппінг ──────────────────────────────────────────────────

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId())
            .sku(p.getSku())
            .name(p.getName())
            .categoryId(p.getCategory().getId())
            .categoryName(p.getCategory().getName())
            .supplierId(p.getSupplier().getId())
            .supplierName(p.getSupplier().getName())
            .unitPrice(p.getUnitPrice())
            .unitOfMeasure(p.getUnitOfMeasure())
            .orderingCost(p.getOrderingCost())
            .holdingCostRate(p.getHoldingCostRate())
            .serviceLevel(p.getServiceLevel())
            .isActive(p.getIsActive())
            .build();
    }

    private Product fromRequest(ProductRequest r, Long companyId) {
        return Product.builder()
            .sku(r.getSku())
            .name(r.getName())
            .unitPrice(r.getUnitPrice())
            .unitOfMeasure(r.getUnitOfMeasure() != null ? r.getUnitOfMeasure() : DEFAULT_UNIT_OF_MEASURE)
            .orderingCost(r.getOrderingCost())
            .holdingCostRate(r.getHoldingCostRate())
            .serviceLevel(r.getServiceLevel())
            .isActive(true)
            .companyId(companyId)
            .category(getCategoryById(r.getCategoryId()))
            .supplier(getSupplierById(r.getSupplierId()))
            .build();
    }
}
