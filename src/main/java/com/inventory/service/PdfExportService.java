package com.inventory.service;

import com.inventory.model.AbcXyzResult;
import com.inventory.repository.AbcXyzResultRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.BaseColor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final AbcXyzResultRepository abcXyzResultRepository;

    @Transactional(readOnly = true)
    public byte[] exportAbcXyzPdf() throws Exception {
        List<AbcXyzResult> data = abcXyzResultRepository.findAllByOrderByRevenueDesc();

        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", BaseFont.EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1252", BaseFont.EMBEDDED);
        Font titleFont  = new Font(bfBold, 16);
        Font headerFont = new Font(bfBold, 10, Font.NORMAL, BaseColor.WHITE);
        Font cellFont   = new Font(bf, 9);

        Paragraph title = new Paragraph("ABC/XYZ Аналіз товарних запасів", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph("Дата: " + LocalDate.now(), cellFont));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1f, 1f, 1.5f, 2f, 1.5f, 5f});

        BaseColor headerColor = new BaseColor(30, 84, 150);
        String[] headers = {"Назва","ABC","XYZ","Оборот","Частка %","CV %","Рекомендація"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        boolean alt = false;
        for (AbcXyzResult r : data) {
            BaseColor rowColor = alt ? new BaseColor(242,247,251) : BaseColor.WHITE;
            alt = !alt;
            String[] vals = {
                r.getProduct().getName(),
                r.getAbcClass(),
                r.getXyzClass(),
                r.getRevenue() != null ? r.getRevenue().toPlainString() : "0",
                r.getRevenueShare() != null ? r.getRevenueShare().multiply(java.math.BigDecimal.valueOf(100)).setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() + "%" : "0%",
                r.getCv() != null ? r.getCv().toPlainString() + "%" : "0%" ,
                r.getCombinedClass()
            };
            for (String v : vals) {
                PdfPCell cell = new PdfPCell(new Phrase(v, cellFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(4);
                table.addCell(cell);
            }
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }
}