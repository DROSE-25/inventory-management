package com.inventory.service;

import com.inventory.dto.request.SupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Supplier;
import com.inventory.repository.SupplierRepository;
import com.inventory.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SecurityUtils      securityUtils;

    public List<SupplierResponse> findAll() {
        Long companyId = securityUtils.getCurrentCompanyId();
        return supplierRepository.findByCompanyId(companyId)
            .stream().map(this::toResponse).toList();
    }

    public SupplierResponse findById(Long id) {
        return toResponse(getSupplierEntityById(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Long companyId = securityUtils.getCurrentCompanyId();
        Supplier supplier = Supplier.builder()
            .name(request.getName())
            .contactPerson(request.getContactPerson())
            .phone(request.getPhone())
            .email(request.getEmail())
            .leadTimeDays(request.getLeadTimeDays())
            .minOrderAmount(request.getMinOrderAmount())
            .isActive(true)
            .companyId(companyId)
            .build();
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        Supplier s = getSupplierEntityById(id);
        s.setIsActive(false);
        supplierRepository.save(s);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest dto) {
        Supplier s = getSupplierEntityById(id);
        s.setName(dto.getName());
        s.setContactPerson(dto.getContactPerson());
        s.setPhone(dto.getPhone());
        s.setEmail(dto.getEmail());
        s.setLeadTimeDays(dto.getLeadTimeDays());
        s.setMinOrderAmount(dto.getMinOrderAmount());
        return toResponse(supplierRepository.save(s));
    }

    // ── Допоміжні методи ────────────────────────────────────────

    public Supplier getSupplierEntityById(Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return supplierRepository.findById(id)
            .filter(s -> s.getCompanyId().equals(companyId))
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    private SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
            .id(s.getId()).name(s.getName())
            .contactPerson(s.getContactPerson()).phone(s.getPhone())
            .email(s.getEmail()).leadTimeDays(s.getLeadTimeDays())
            .minOrderAmount(s.getMinOrderAmount()).isActive(s.getIsActive())
            .build();
    }
}
