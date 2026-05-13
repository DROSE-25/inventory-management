package com.inventory.analysis;

import com.inventory.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbcXyzAnalyzerTest {

    @InjectMocks private AbcAnalyzer abcAnalyzer;

    @Mock private SaleRepository saleRepository;
    @InjectMocks private XyzAnalyzer xyzAnalyzer;

    // ── ABC ────────────────────────────────────────────────────────────────

    @Test
    void abc_classifiesCorrectly() {
        Map<Long, BigDecimal> revenue = Map.of(
            1L, BigDecimal.valueOf(8000),  // 80% → A
            2L, BigDecimal.valueOf(1500),  // 95% → B
            3L, BigDecimal.valueOf(500)    //      → C
        );
        Map<Long, String> result = abcAnalyzer.analyze(revenue);

        assertThat(result.get(1L)).isEqualTo("A");
        assertThat(result.get(2L)).isEqualTo("B");
        assertThat(result.get(3L)).isEqualTo("C");
    }

    @Test
    void abc_singleProduct_isA() {
        Map<Long, BigDecimal> revenue = Map.of(1L, BigDecimal.valueOf(5000));
        Map<Long, String> result = abcAnalyzer.analyze(revenue);
        assertThat(result.get(1L)).isEqualTo("A");
    }

    @Test
    void abc_allZeroRevenue_returnsC() {
        Map<Long, BigDecimal> revenue = Map.of(
            1L, BigDecimal.ZERO,
            2L, BigDecimal.ZERO
        );
        Map<Long, String> result = abcAnalyzer.analyze(revenue);
        assertThat(result.values()).containsOnly("C");
    }

    // ── XYZ ───────────────────────────────────────────────────────────────

    @Test
    void xyz_stableData_returnsX() {
        when(saleRepository.findMonthlyDemand(1L)).thenReturn(List.of(
            new Object[]{"2024-01", 100},
            new Object[]{"2024-02", 100},
            new Object[]{"2024-03", 100},
            new Object[]{"2024-04", 100}
        ));
        assertThat(xyzAnalyzer.analyze(1L)).isEqualTo("X"); // CV = 0%
    }

    @Test
    void xyz_moderateVariation_returnsY() {
        when(saleRepository.findMonthlyDemand(2L)).thenReturn(List.of(
            new Object[]{"2024-01", 80},
            new Object[]{"2024-02", 120},
            new Object[]{"2024-03", 90},
            new Object[]{"2024-04", 110}
        ));
        // mean=100, std≈17.6, CV≈17.6% → Y
        assertThat(xyzAnalyzer.analyze(2L)).isEqualTo("Y");
    }

    @Test
    void xyz_highVariation_returnsZ() {
        when(saleRepository.findMonthlyDemand(3L)).thenReturn(List.of(
            new Object[]{"2024-01", 5},
            new Object[]{"2024-02", 200},
            new Object[]{"2024-03", 3},
            new Object[]{"2024-04", 180}
        ));
        assertThat(xyzAnalyzer.analyze(3L)).isEqualTo("Z");
    }

    @Test
    void xyz_zeroMean_returnsZ_noException() {
        when(saleRepository.findMonthlyDemand(4L)).thenReturn(List.of(
            new Object[]{"2024-01", 0},
            new Object[]{"2024-02", 0}
        ));
        assertThat(xyzAnalyzer.analyze(4L)).isEqualTo("Z"); // без NPE/ArithmeticException
    }

    @Test
    void xyz_notEnoughData_returnsZ() {
        List<Object[]> oneMonth = new ArrayList<>();
    oneMonth.add(new Object[]{"2024-01", 50});
    when(saleRepository.findMonthlyDemand(5L)).thenReturn(oneMonth);
        assertThat(xyzAnalyzer.analyze(5L)).isEqualTo("Z");
    }
}