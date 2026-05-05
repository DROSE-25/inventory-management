package com.inventory.service;
 
import com.inventory.dto.request.ProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.exception.ValidationException;
import com.inventory.model.Product;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
 
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
 
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }
 
    public ProductResponse findById(Long id) {
        return toResponse(productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }
 
    public ProductResponse findBySku(String sku) {
        return toResponse(productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product з SKU " + sku + " не знайдено")));
    }
 
    public List<ProductResponse> search(String name) {
        return productRepository.searchByName(name).stream().map(this::toResponse).toList();
    }
 
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new ValidationException("Товар з SKU " + request.getSku() + " вже існує");
        }
        Product product = fromRequest(request);
        return toResponse(productRepository.save(product));
    }
 
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getOrderingCost() != null) product.setOrderingCost(request.getOrderingCost());
        if (request.getHoldingCostRate() != null) product.setHoldingCostRate(request.getHoldingCostRate());
        if (request.getServiceLevel() != null) product.setServiceLevel(request.getServiceLevel());
        product.setCategory(categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId())));
        product.setSupplier(supplierRepository.findById(request.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId())));
        return toResponse(productRepository.save(product));
    }
 
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setIsActive(false);  // м'яке видалення
        productRepository.save(product);
    }
 
    // ── маппінг ──────────────────────────────────────────
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
            .unitOfMeasure(r.getUnitOfMeasure() != null ? r.getUnitOfMeasure() : "шт")
            .orderingCost(r.getOrderingCost())
            .holdingCostRate(r.getHoldingCostRate())
            .serviceLevel(r.getServiceLevel())
            .isActive(true)
            .category(categoryRepository.findById(r.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", r.getCategoryId())))
            .supplier(supplierRepository.findById(r.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", r.getSupplierId())))
            .build();
    }
}
