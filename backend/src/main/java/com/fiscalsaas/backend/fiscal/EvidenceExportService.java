package com.fiscalsaas.backend.fiscal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.CompanyRepository;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessDeniedException;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.invoices.FiscalInvoice;
import com.fiscalsaas.backend.invoices.FiscalInvoiceRepository;
import com.fiscalsaas.backend.verifactu.SifRecord;
import com.fiscalsaas.backend.verifactu.SifRecordRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceExportService {

	private static final Set<FiscalRole> EXPORT_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT,
			FiscalRole.AUDITOR);

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;
	private final FiscalInvoiceRepository invoices;
	private final SifRecordRepository sifRecords;
	private final AuditEventRepository auditEvents;
	private final EvidenceExportJobRepository jobs;

	EvidenceExportService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			FiscalInvoiceRepository invoices,
			SifRecordRepository sifRecords,
			AuditEventRepository auditEvents,
			EvidenceExportJobRepository jobs) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.invoices = invoices;
		this.sifRecords = sifRecords;
		this.auditEvents = auditEvents;
		this.jobs = jobs;
	}

	@Transactional(readOnly = true)
	public List<EvidenceExportResponse> listExports(String tenantId, String companyId, HttpServletRequest request) {
		requireExportAccess(tenantId, request);
		requireCompany(tenantId, companyId);
		return jobs.findByTenant_IdAndCompany_IdOrderByCreatedAtDesc(tenantId, companyId)
				.stream()
				.map(EvidenceExportResponse::from)
				.toList();
	}

	@Transactional
	public EvidenceExportResponse createExport(String tenantId, String companyId, HttpServletRequest request) {
		Membership membership = requireExportAccess(tenantId, request);
		Company company = requireCompany(tenantId, companyId);
		byte[] zip = renderZip(tenantId, company);
		String sha256 = sha256(zip);
		EvidenceExportJob job = jobs.save(EvidenceExportJob.completed(membership.tenant(), company, tenantAccess.currentUser().id(), "{}", sha256));
		return EvidenceExportResponse.from(job);
	}

	@Transactional(readOnly = true)
	public EvidenceExportDownload downloadExport(String tenantId, String companyId, String exportId, HttpServletRequest request) {
		requireExportAccess(tenantId, request);
		EvidenceExportJob job = jobs.findByIdAndTenant_IdAndCompany_Id(exportId, tenantId, companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Evidence export was not found for the company."));
		byte[] bytes = renderZip(tenantId, job.company());
		return new EvidenceExportDownload("evidence-pack-" + exportId + ".zip", bytes, sha256(bytes));
	}

	private byte[] renderZip(String tenantId, Company company) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
				String readme = "Paquete de evidencia local/preprod. No acredita cumplimiento legal productivo ni envio a AEAT.\n"
						+ "GeneratedAt=" + Instant.now() + "\n"
						+ "Company=" + company.legalName() + "\n";
				add(zip, "README_LOCAL_PREPROD.txt", readme);
				add(zip, "invoices.csv", invoicesCsv(tenantId, company.id()));
				add(zip, "sif-records.json", sifJson(tenantId));
				add(zip, "audit-events.json", auditJson(tenantId, company.id()));
				add(zip, "checksums.sha256", "generated-dynamically\n");
			}
			return bytes.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to render evidence export.", exception);
		}
	}

	private String invoicesCsv(String tenantId, String companyId) {
		StringBuilder csv = new StringBuilder("id,invoiceNumber,fiscalNumber,status,total,paymentStatus\n");
		for (FiscalInvoice invoice : invoices.searchByTenant(tenantId, companyId, null, null)) {
			csv.append(invoice.id()).append(',')
					.append(invoice.invoiceNumber()).append(',')
					.append(invoice.fiscalNumber()).append(',')
					.append(invoice.status()).append(',')
					.append(invoice.payableTotal()).append(',')
					.append(invoice.paymentStatus()).append('\n');
		}
		return csv.toString();
	}

	private String sifJson(String tenantId) {
		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (SifRecord record : sifRecords.findByTenant_IdOrderBySequenceNumberAsc(tenantId)) {
			if (!first) {
				json.append(',');
			}
			first = false;
			json.append("{\"id\":\"").append(record.id()).append("\",\"recordType\":\"").append(record.recordType())
					.append("\",\"hash\":\"").append(record.recordHash()).append("\"}");
		}
		return json.append(']').toString();
	}

	private String auditJson(String tenantId, String companyId) {
		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (AuditEvent event : auditEvents.findByTenant_IdAndCompany_IdOrderByOccurredAtDesc(tenantId, companyId)) {
			if (!first) {
				json.append(',');
			}
			first = false;
			json.append("{\"eventType\":\"").append(event.eventType()).append("\",\"entityId\":\"")
					.append(event.entityId()).append("\",\"hash\":\"").append(event.eventHash()).append("\"}");
		}
		return json.append(']').toString();
	}

	private void add(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	private Membership requireExportAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!EXPORT_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot export evidence.");
		}
		return membership;
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
	}

	private String sha256(byte[] value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}
}
