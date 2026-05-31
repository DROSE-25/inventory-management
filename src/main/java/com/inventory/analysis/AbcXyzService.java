package com.inventory.analysis;

import com.inventory.analysis.dto.AbcXyzResponse;
import com.inventory.model.AbcXyzResult;
import com.inventory.model.Product;
import com.inventory.repository.AbcXyzResultRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbcXyzService {

    private final AbcAnalyzer            abcAnalyzer;
    private final XyzAnalyzer            xyzAnalyzer;
    private final ProductRepository      productRepository;
    private final SaleRepository         saleRepository;
    private final AbcXyzResultRepository abcXyzResultRepository;
    private final SecurityUtils          securityUtils;

    private static final Map<String, String> RECOMMENDATIONS = Map.of(
        "AX", "Пріоритетний товар зі стабільним попитом. Мінімальний страховий запас, точне планування.",
        "AY", "Пріоритетний товар з помірними коливаннями. Враховуйте тренд при прогнозуванні.",
        "AZ", "Пріоритетний товар з нестабільним попитом. Підвищений страховий запас, постійний моніторинг.",
        "BX", "Товар середньої важливості зі стабільним попитом. Стандартний EOQ.",
        "BY", "Товар середньої важливості з помірними коливаннями. Стандартний контроль.",
        "BZ", "Товар середньої важливості з нестабільним попитом. Підвищений страховий запас.",
        "CX", "Низькопріоритетний товар зі стабільним попитом. Замовляти рідко великими партіями.",
        "CY", "Низькопріоритетний товар з помірними коливаннями. Мінімальні запаси.",
        "CZ", "Проблемний товар. Розглянути замовлення під конкретну потребу або виключення з асортименту."
    );

    @Transactional(readOnly = true)
    public List<AbcXyzResponse> getAll() {
        Long companyId = securityUtils.getCurrentCompanyId();
        return abcXyzResultRepository.findAllByCompanyIdOrderByRevenueDesc(companyId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbcXyzResponse> getByAbcClass(String abcClass) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return abcXyzResultRepository.findByAbcClassAndCompanyId(abcClass, companyId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbcXyzResponse> getByXyzClass(String xyzClass) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return abcXyzResultRepository.findByXyzClassAndCompanyId(xyzClass, companyId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<AbcXyzResponse> recalculate() {
        Long companyId = securityUtils.getCurrentCompanyId();
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusMonths(12);

        log.info("Recalculating ABC/XYZ for company={}, period {} – {}", companyId, from, to);

        abcXyzResultRepository.deleteByPeriodAndCompanyId(from, to, companyId);

        List<Product> products = productRepository.findByCompanyId(companyId);

        Map<Long, BigDecimal> revenueMap = products.stream()
            .collect(Collectors.toMap(
                Product::getId,
                p -> calcRevenue(p.getId(), from, to, companyId)
            ));

        Map<Long, String>     abcClasses = abcAnalyzer.analyze(revenueMap);
        Map<Long, BigDecimal> shares     = abcAnalyzer.calcRevenueShares(revenueMap);

        List<AbcXyzResult> results = products.stream().map(product -> {
            Long   id       = product.getId();
            String abcClass = abcClasses.getOrDefault(id, "C");
            String xyzClass = xyzAnalyzer.analyze(id);
            String combined = abcClass + xyzClass;
            double cvValue  = xyzAnalyzer.calcCv(id);

            return AbcXyzResult.builder()
                .product(product)
                .abcClass(abcClass)
                .xyzClass(xyzClass)
                .combinedClass(combined)
                .revenue(revenueMap.getOrDefault(id, BigDecimal.ZERO))
                .revenueShare(shares.getOrDefault(id, BigDecimal.ZERO))
                .cv(BigDecimal.valueOf(cvValue).setScale(2, RoundingMode.HALF_UP))
                .periodFrom(from)
                .periodTo(to)
                .calculatedAt(OffsetDateTime.now())
                .companyId(companyId)
                .build();
        }).collect(Collectors.toList());

        abcXyzResultRepository.saveAll(results);
        log.info("ABC/XYZ analysis saved: {} products for company {}", results.size(), companyId);

        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Приватні хелпери ────────────────────────────────────────────────────

    private BigDecimal calcRevenue(Long productId, LocalDate from, LocalDate to, Long companyId) {
        return saleRepository.aggregateByPeriod("month", productId, null, from, to, companyId)
            .stream()
            .map(row -> row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AbcXyzResponse toResponse(AbcXyzResult r) {
        String combined = r.getCombinedClass();
        return AbcXyzResponse.builder()
            .productId(r.getProduct().getId())
            .productName(r.getProduct().getName())
            .sku(r.getProduct().getSku())
            .abcClass(r.getAbcClass())
            .xyzClass(r.getXyzClass())
            .combinedClass(combined)
            .revenue(r.getRevenue())
            .revenueShare(r.getRevenueShare())
            .cv(r.getCv())
            .recommendation(RECOMMENDATIONS.getOrDefault(combined, "Немає рекомендацій"))
            .build();
    }
}