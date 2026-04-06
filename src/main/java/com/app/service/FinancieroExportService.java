package com.app.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.app.dto.ReportePagoFilaDTO;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class FinancieroExportService {

	private static final NumberFormat NF_CL = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));

	public byte[] generarPdfReporte(String clubNombre, String periodo, List<ReportePagoFilaDTO> filas) {
		try {
			com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
			com.lowagie.text.Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
			com.lowagie.text.Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

			Document doc = new Document(PageSize.A4.rotate(), 28, 28, 36, 36);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(doc, baos);
			doc.open();

			doc.add(new Paragraph("AdminClub — Reporte de pagos", titleFont));
			doc.add(new Paragraph("Club: " + nullToEmpty(clubNombre), normal));
			doc.add(new Paragraph("Período: " + nullToEmpty(periodo), normal));
			doc.add(new Paragraph(" ", normal));

			PdfPTable table = new PdfPTable(7);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 2.2f, 1.4f, 1.3f, 1f, 1f, 1f, 1.5f });

			String[] headers = { "Deportista", "Categoría", "Período cuota", "Estado", "Medio", "Monto", "Fecha registro" };
			for (String h : headers) {
				PdfPCell c = new PdfPCell(new Phrase(h, smallBold));
				c.setBackgroundColor(Color.LIGHT_GRAY);
				c.setHorizontalAlignment(1); // Element.ALIGN_CENTER
				c.setPadding(4);
				table.addCell(c);
			}

			for (ReportePagoFilaDTO f : filas) {
				addCell(table, nullToEmpty(f.getDeportista()), normal);
				addCell(table, nullToEmpty(f.getCategoria()), normal);
				addCell(table, nullToEmpty(f.getPeriodoCuota()), normal);
				addCell(table, nullToEmpty(f.getEstado()), normal);
				addCell(table, nullToEmpty(f.getMedio()), normal);
				addCell(table, NF_CL.format(f.getMonto()), normal);
				addCell(table, nullToEmpty(f.getFechaRegistro()), normal);
			}

			doc.add(table);
			doc.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Error generando PDF", e);
		}
	}

	private void addCell(PdfPTable table, String text, com.lowagie.text.Font font) {
		PdfPCell c = new PdfPCell(new Phrase(text, font));
		c.setPadding(3);
		table.addCell(c);
	}

	public byte[] generarExcelReporte(String clubNombre, String periodo, List<ReportePagoFilaDTO> filas) {
		try (Workbook wb = new XSSFWorkbook()) {
			Sheet sh = wb.createSheet("Pagos");

			CellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			Font hf = wb.createFont();
			hf.setBold(true);
			headerStyle.setFont(hf);

			CellStyle dataStyle = wb.createCellStyle();
			dataStyle.setBorderBottom(BorderStyle.THIN);

			int r = 0;
			Row r0 = sh.createRow(r++);
			r0.createCell(0).setCellValue("AdminClub — Reporte de pagos");
			Row r1 = sh.createRow(r++);
			r1.createCell(0).setCellValue("Club: " + clubNombre);
			Row r2 = sh.createRow(r++);
			r2.createCell(0).setCellValue("Período: " + periodo);
			r++;

			String[] headers = { "Deportista", "Categoría", "Período cuota", "Estado", "Medio", "Monto", "Fecha registro" };
			Row hr = sh.createRow(r++);
			for (int i = 0; i < headers.length; i++) {
				Cell c = hr.createCell(i);
				c.setCellValue(headers[i]);
				c.setCellStyle(headerStyle);
			}

			for (ReportePagoFilaDTO f : filas) {
				Row row = sh.createRow(r++);
				int c = 0;
				put(row, c++, f.getDeportista(), dataStyle);
				put(row, c++, f.getCategoria(), dataStyle);
				put(row, c++, f.getPeriodoCuota(), dataStyle);
				put(row, c++, f.getEstado(), dataStyle);
				put(row, c++, f.getMedio(), dataStyle);
				Cell m = row.createCell(c++);
				m.setCellValue(f.getMonto());
				m.setCellStyle(dataStyle);
				put(row, c++, f.getFechaRegistro(), dataStyle);
			}

			for (int i = 0; i < 7; i++) {
				sh.autoSizeColumn(i);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Error generando Excel", e);
		}
	}

	private void put(Row row, int idx, String val, CellStyle st) {
		Cell c = row.createCell(idx);
		c.setCellValue(val != null ? val : "");
		c.setCellStyle(st);
	}

	private static String nullToEmpty(String s) {
		return s != null ? s : "";
	}
}
