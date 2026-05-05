package com.inventory.service;
 
import com.inventory.dto.request.SupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Supplier;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {
 
    private final SupplierRepository supplierRepository;
 
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }
 
    public SupplierResponse findById(Long id) {
        return toResponse(supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", id)));
    }
 
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
            .name(request.getName())
            .contactPerson(request.getContactPerson())
            .phone(request.getPhone())
            .email(request.getEmail())
            .leadTimeDays(request.getLeadTimeDays())
            .minOrderAmount(request.getMinOrderAmount())
            .isActive(true)
            .build();
        return toResponse(supplierRepository.save(supplier));
    }
 
    @Transactional
    public void delete(Long id) {
        Supplier s = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        s.setIsActive(false);
        supplierRepository.save(s);
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
