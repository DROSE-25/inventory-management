package com.inventory.service;

import com.inventory.dto.request.ProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.exception.ValidationException;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j // Додано для логування
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    // Виносимо магічний рядок у константу
    private static final String DEFAULT_UNIT_OF_MEASURE = "шт";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public Page<ProductResponse> findAll(Pageable pageable) {
        log.debug("Отримання списку активних товарів, сторінка: {}", pageable.getPageNumber());
        return productRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    public ProductResponse findById(Long id) {
        return toResponse(getProductEntityById(id));
    }

    public ProductResponse findBySku(String sku) {
        return toResponse(productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Товар з SKU '" + sku + "' не знайдено")));
    }

    public List<ProductResponse> search(String name) {
        log.debug("Пошук товарів за назвою: {}", name);
        return productRepository.searchByName(name).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Спроба створення нового товару з SKU: {}", request.getSku());
        
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            log.warn("Помилка створення: товар з SKU '{}' вже існує", request.getSku());
            throw new ValidationException("Товар з SKU '" + request.getSku() + "' вже існує");
        }
        
        Product product = fromRequest(request);
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

        if (request.getOrderingCost() != null) product.setOrderingCost(request.getOrderingCost());
        if (request.getHoldingCostRate() != null) product.setHoldingCostRate(request.getHoldingCostRate());
        if (request.getServiceLevel() != null) product.setServiceLevel(request.getServiceLevel());

        // Використовуємо DRY-методи
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
        product.setIsActive(false); // м'яке видалення
        productRepository.save(product);
        log.info("Товар з ID: {} успішно деактивовано", id);
    }

    // ── Допоміжні методи (DRY - Don't Repeat Yourself) ───────────

    private Product getProductEntityById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));
    }

    private Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
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

    private Product fromRequest(ProductRequest r) {
        return Product.builder()
            .sku(r.getSku())
            .name(r.getName())
            .unitPrice(r.getUnitPrice())
            .unitOfMeasure(r.getUnitOfMeasure() != null ? r.getUnitOfMeasure() : DEFAULT_UNIT_OF_MEASURE)
            .orderingCost(r.getOrderingCost())
            .holdingCostRate(r.getHoldingCostRate())
            .serviceLevel(r.getServiceLevel())
            .isActive(true)
            .category(getCategoryById(r.getCategoryId()))
            .supplier(getSupplierById(r.getSupplierId()))
            .build();
    }
}