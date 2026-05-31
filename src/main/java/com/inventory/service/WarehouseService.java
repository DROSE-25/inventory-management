package com.inventory.service;

import com.inventory.dto.request.WarehouseRequest;
import com.inventory.dto.response.WarehouseResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Warehouse;
import com.inventory.repository.WarehouseRepository;
import com.inventory.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final SecurityUtils       securityUtils;

    public List<WarehouseResponse> findAll() {
        Long companyId = securityUtils.getCurrentCompanyId();
        return warehouseRepository.findByCompanyId(companyId)
            .stream().map(this::toResponse).toList();
    }

    public WarehouseResponse findById(Long id) {
        return toResponse(getWarehouseEntityById(id));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        Long companyId = securityUtils.getCurrentCompanyId();
        Warehouse w = Warehouse.builder()
            .name(request.getName())
            .address(request.getAddress())
            .capacity(request.getCapacity())
            .isActive(true)
            .companyId(companyId)
            .build();
        return toResponse(warehouseRepository.save(w));
    }

    @Transactional
    public void delete(Long id) {
        getWarehouseEntityById(id); // перевірка що belongs до цієї компанії
        warehouseRepository.deleteById(id);
    }

    // ── Допоміжні методи ────────────────────────────────────────

    public Warehouse getWarehouseEntityById(Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return warehouseRepository.findById(id)
            .filter(w -> w.getCompanyId().equals(companyId))
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return WarehouseResponse.builder()
            .id(w.getId()).name(w.getName())
            .address(w.getAddress()).capacity(w.getCapacity())
            .isActive(w.getIsActive()).build();
    }
}
