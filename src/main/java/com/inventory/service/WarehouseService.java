package com.inventory.service;

import com.inventory.dto.request.WarehouseRequest;
import com.inventory.dto.response.WarehouseResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Warehouse;
import com.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<WarehouseResponse> findAll() {
        return warehouseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public WarehouseResponse findById(Long id) {
        return toResponse(warehouseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id)));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        Warehouse w = Warehouse.builder()
            .name(request.getName())
            .address(request.getAddress())
            .capacity(request.getCapacity())
            .isActive(true)
            .build();
        return toResponse(warehouseRepository.save(w));
    }

    @Transactional
    public void delete(Long id) {
        warehouseRepository.deleteById(id);
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return WarehouseResponse.builder()
            .id(w.getId()).name(w.getName())
            .address(w.getAddress()).capacity(w.getCapacity())
            .isActive(w.getIsActive()).build();
    }
}