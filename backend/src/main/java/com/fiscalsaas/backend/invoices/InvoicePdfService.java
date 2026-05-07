package com.fiscalsaas.backend.invoices;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class InvoicePdfService {

	InvoicePdfDownload render(FiscalInvoice invoice, List<FiscalInvoiceLine> lines) {
		String filename = (invoice.fiscalNumber() == null ? invoice.invoiceNumber() : invoice.fiscalNumber())
				.replaceAll("[^A-Za-z0-9_.-]", "_") + ".pdf";
		StringBuilder text = new StringBuilder();
		text.append("Factura fiscal local/preprod\\n");
		text.append("Numero fiscal: ").append(nullToDash(invoice.fiscalNumber())).append("\\n");
		text.append("Referencia borrador: ").append(invoice.invoiceNumber()).append("\\n");
		text.append("Estado: ").append(invoice.status()).append("\\n");
		text.append("Emisor: ").append(invoice.issuerCompany().legalName()).append(" ").append(invoice.issuerCompany().taxId()).append("\\n");
		text.append("Cliente: ").append(invoice.customerCompany().legalName()).append(" ").append(invoice.customerCompany().taxId()).append("\\n");
		text.append("Fecha emision: ").append(invoice.issueDate()).append("\\n");
		text.append("Vencimiento: ").append(nullToDash(invoice.dueDate())).append("\\n");
		text.append("Lineas:\\n");
		for (FiscalInvoiceLine line : lines) {
			text.append(line.lineNumber()).append(". ")
					.append(line.description()).append(" qty=").append(line.quantity())
					.append(" price=").append(line.unitPrice())
					.append(" base=").append(line.lineBase())
					.append(" iva=").append(line.taxAmount())
					.append(" total=").append(line.lineTotal())
					.append("\\n");
		}
		text.append("Base: ").append(invoice.taxableBase()).append("\\n");
		text.append("IVA: ").append(invoice.taxTotal()).append("\\n");
		text.append("Retencion: ").append(invoice.withholdingTotal()).append("\\n");
		text.append("Total a pagar: ").append(invoice.payableTotal()).append("\\n");
		text.append("Registro local de pruebas, sin envio legal a AEAT ni certificacion productiva.\\n");
		byte[] bytes = minimalPdf(text.toString());
		return new InvoicePdfDownload(filename, bytes, sha256(bytes));
	}

	private byte[] minimalPdf(String text) {
		String escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
				.replace("\r", "")
				.replace("\n", ") Tj T* (");
		String stream = "BT /F1 10 Tf 50 780 Td 14 TL (" + escaped + ") Tj ET";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String header = "%PDF-1.4\n";
		write(out, header);
		int obj1 = out.size();
		write(out, "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
		int obj2 = out.size();
		write(out, "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
		int obj3 = out.size();
		write(out, "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
		int obj4 = out.size();
		write(out, "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
		int obj5 = out.size();
		write(out, "5 0 obj << /Length " + stream.getBytes(StandardCharsets.ISO_8859_1).length + " >> stream\n");
		write(out, stream + "\nendstream endobj\n");
		int xref = out.size();
		write(out, "xref\n0 6\n0000000000 65535 f \n");
		for (int offset : List.of(obj1, obj2, obj3, obj4, obj5)) {
			write(out, String.format("%010d 00000 n \n", offset));
		}
		write(out, "trailer << /Size 6 /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
		return out.toByteArray();
	}

	private void write(ByteArrayOutputStream out, String value) {
		out.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
	}

	private String sha256(byte[] value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}

	private String nullToDash(Object value) {
		return value == null ? "-" : value.toString();
	}
}
