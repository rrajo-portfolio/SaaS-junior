package com.fiscalsaas.backend.verifactu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiscalsaas.backend.api.ApiConflictException;
import com.fiscalsaas.backend.api.ApiValidationException;
import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessDeniedException;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.invoices.FiscalInvoice;
import com.fiscalsaas.backend.invoices.FiscalInvoiceRepository;
import com.fiscalsaas.backend.invoices.InvoiceStatus;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SifRecordService {

	private static final String GENESIS_HASH = "GENESIS";
	private static final String SYSTEM_VERSION = "0.1.0";
	private static final String NORMATIVE_VERSION = "CURRENT_AS_OF_2026_05_03";
	private static final Set<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);

	private final TenantAccessService tenantAccess;
	private final FiscalInvoiceRepository invoices;
	private final SifRecordRepository records;
	private final SifRecordHashChainRepository hashChain;
	private final SifEventLogRepository events;
	private final SifExportBatchRepository exports;
	private final ObjectMapper objectMapper = new ObjectMapper();

	SifRecordService(
			TenantAccessService tenantAccess,
			FiscalInvoiceRepository invoices,
			SifRecordRepository records,
			SifRecordHashChainRepository hashChain,
			SifEventLogRepository events,
			SifExportBatchRepository exports) {
		this.tenantAccess = tenantAccess;
		this.invoices = invoices;
		this.records = records;
		this.hashChain = hashChain;
		this.events = events;
		this.exports = exports;
	}

	@Transactional(readOnly = true)
	public List<SifRecordResponse> listRecords(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return records.findByTenant_IdOrderBySequenceNumberDesc(tenantId)
				.stream()
				.map(SifRecordResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<SifEventResponse> listRecordEvents(String tenantId, String recordId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireRecord(tenantId, recordId);
		return events.findByTenant_IdAndRecord_IdOrderByEventAtAsc(tenantId, recordId)
				.stream()
				.map(SifEventResponse::from)
				.toList();
	}

	@Transactional
	public SifRecordResponse registerInvoice(String tenantId, CreateSifRecordRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		FiscalInvoice invoice = requireInvoice(tenantId, body.invoiceId());
		if (!InvoiceStatus.ISSUED.name().equals(invoice.status())) {
			throw new ApiValidationException("Only ISSUED invoices can be registered in SIF.");
		}
		if (records.existsByTenant_IdAndInvoice_IdAndRecordType(tenantId, invoice.id(), SifRecordType.REGISTRATION.name())) {
			throw new ApiConflictException("Invoice already has a SIF registration record.");
		}
		return SifRecordResponse.from(createRecord(membership, invoice, null, SifRecordType.REGISTRATION, null));
	}

	@Transactional
	public SifRecordResponse cancelRecord(String tenantId, String recordId, CancelSifRecordRequest body, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		SifRecord source = requireRecord(tenantId, recordId);
		if (SifRecordType.CANCELLATION.name().equals(source.recordType())) {
			throw new ApiValidationException("A cancellation record cannot be cancelled.");
		}
		if (records.existsByTenant_IdAndSourceRecord_IdAndRecordType(tenantId, source.id(), SifRecordType.CANCELLATION.name())) {
			throw new ApiConflictException("SIF registration record is already cancelled.");
		}
		String reason = body == null || body.reason() == null || body.reason().isBlank()
				? "Cancellation requested"
				: body.reason().trim();
		return SifRecordResponse.from(createRecord(membership, source.invoice(), source, SifRecordType.CANCELLATION, reason));
	}

	@Transactional
	public SifExportBatchResponse createExport(String tenantId, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		List<SifRecord> tenantRecords = records.findByTenant_IdOrderBySequenceNumberAsc(tenantId);
		if (tenantRecords.isEmpty()) {
			throw new ApiValidationException("At least one SIF record is required to create an export.");
		}
		Instant exportedAt = Instant.now();
		String payload = writeJson(exportPayload(tenantId, tenantRecords, exportedAt));
		SifExportBatch batch = exports.save(SifExportBatch.create(
				membership.tenant(),
				tenantRecords.getFirst().sequenceNumber(),
				tenantRecords.getLast().sequenceNumber(),
				tenantRecords.size(),
				sha256(payload),
				payload,
				tenantAccess.currentUser().id(),
				exportedAt));
		events.save(SifEventLog.create(
				tenantRecords.getLast(),
				SifEventType.EXPORT_CREATED,
				tenantAccess.currentUser().id(),
				"Export batch " + batch.id() + " created",
				exportedAt));
		return SifExportBatchResponse.from(batch);
	}

	@Transactional(readOnly = true)
	public List<SifExportBatchResponse> listExports(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return exports.findByTenant_IdOrderByCreatedAtDesc(tenantId)
				.stream()
				.map(SifExportBatchResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public SifExportBatchResponse getExport(String tenantId, String exportId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return exports.findByIdAndTenant_Id(exportId, tenantId)
				.map(SifExportBatchResponse::from)
				.orElseThrow(() -> new ResourceNotFoundException("SIF export was not found in the tenant."));
	}

	@Transactional(readOnly = true)
	public SifHashChainVerificationResponse verifyHashChain(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		List<SifRecord> tenantRecords = records.findByTenant_IdOrderBySequenceNumberAsc(tenantId);
		String previous = GENESIS_HASH;
		long expectedSequence = 1;
		for (SifRecord record : tenantRecords) {
			if (record.sequenceNumber() != expectedSequence) {
				return new SifHashChainVerificationResponse(false, tenantRecords.size(), previous);
			}
			if (!previous.equals(record.previousHash())) {
				return new SifHashChainVerificationResponse(false, tenantRecords.size(), previous);
			}
			String recomputedHash = hashRecord(previous, record.canonicalPayload());
			if (!recomputedHash.equals(record.recordHash())) {
				return new SifHashChainVerificationResponse(false, tenantRecords.size(), previous);
			}
			previous = record.recordHash();
			expectedSequence++;
		}
		return new SifHashChainVerificationResponse(true, tenantRecords.size(), previous);
	}

	private SifRecord createRecord(
			Membership membership,
			FiscalInvoice invoice,
			SifRecord sourceRecord,
			SifRecordType recordType,
			String reason) {
		String tenantId = membership.tenant().id();
		List<SifRecord> lastRecords = records.findLastForTenantForUpdate(tenantId, PageRequest.of(0, 1));
		SifRecord lastRecord = lastRecords.isEmpty() ? null : lastRecords.getFirst();
		long sequenceNumber = lastRecord == null ? 1 : lastRecord.sequenceNumber() + 1;
		String previousHash = lastRecord == null ? GENESIS_HASH : lastRecord.recordHash();
		Instant createdAt = Instant.now();
		String canonicalPayload = writeJson(canonicalPayload(
				tenantId,
				invoice,
				sourceRecord,
				recordType,
				sequenceNumber,
				previousHash,
				reason,
				createdAt));
		String recordHash = hashRecord(previousHash, canonicalPayload);
		SifRecord record = records.save(SifRecord.create(
				membership.tenant(),
				invoice,
				sourceRecord,
				recordType,
				sequenceNumber,
				previousHash,
				recordHash,
				canonicalPayload,
				SYSTEM_VERSION,
				NORMATIVE_VERSION,
				tenantAccess.currentUser().id(),
				createdAt));
		hashChain.save(SifRecordHashChain.create(record));
		events.save(SifEventLog.create(
				record,
				recordType == SifRecordType.REGISTRATION ? SifEventType.RECORD_REGISTERED : SifEventType.RECORD_CANCELLED,
				tenantAccess.currentUser().id(),
				recordType == SifRecordType.REGISTRATION ? "SIF registration record created" : "SIF cancellation record created",
				createdAt));
		return record;
	}

	private Map<String, Object> canonicalPayload(
			String tenantId,
			FiscalInvoice invoice,
			SifRecord sourceRecord,
			SifRecordType recordType,
			long sequenceNumber,
			String previousHash,
			String reason,
			Instant createdAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("recordType", recordType.name());
		payload.put("sequenceNumber", sequenceNumber);
		payload.put("tenantId", tenantId);
		payload.put("invoiceId", invoice.id());
		payload.put("invoiceNumber", invoice.invoiceNumber());
		payload.put("fiscalNumber", invoice.fiscalNumber());
		payload.put("seriesCode", invoice.seriesCode());
		payload.put("invoiceType", invoice.invoiceType());
		payload.put("invoiceStatus", invoice.status());
		payload.put("issueDate", invoice.issueDate().toString());
		payload.put("currency", invoice.currency());
		payload.put("taxableBase", invoice.taxableBase());
		payload.put("taxTotal", invoice.taxTotal());
		payload.put("total", invoice.total());
		payload.put("issuerTaxId", invoice.issuerCompany().taxId());
		payload.put("customerTaxId", invoice.customerCompany().taxId());
		payload.put("sourceRecordId", sourceRecord == null ? null : sourceRecord.id());
		payload.put("sourceRecordHash", sourceRecord == null ? null : sourceRecord.recordHash());
		payload.put("previousHash", previousHash);
		payload.put("systemVersion", SYSTEM_VERSION);
		payload.put("normativeVersion", NORMATIVE_VERSION);
		payload.put("createdAt", createdAt.toString());
		payload.put("reason", reason);
		return payload;
	}

	private Map<String, Object> exportPayload(String tenantId, List<SifRecord> tenantRecords, Instant exportedAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("tenantId", tenantId);
		payload.put("systemVersion", SYSTEM_VERSION);
		payload.put("normativeVersion", NORMATIVE_VERSION);
		payload.put("exportedAt", exportedAt.toString());
		payload.put("recordCount", tenantRecords.size());
		payload.put("recordFromSequence", tenantRecords.getFirst().sequenceNumber());
		payload.put("recordToSequence", tenantRecords.getLast().sequenceNumber());
		List<Map<String, Object>> exportedRecords = new ArrayList<>();
		for (SifRecord record : tenantRecords.stream().sorted(Comparator.comparingLong(SifRecord::sequenceNumber)).toList()) {
			Map<String, Object> exportedRecord = new LinkedHashMap<>();
			exportedRecord.put("id", record.id());
			exportedRecord.put("recordType", record.recordType());
			exportedRecord.put("sequenceNumber", record.sequenceNumber());
			exportedRecord.put("previousHash", record.previousHash());
			exportedRecord.put("recordHash", record.recordHash());
			exportedRecord.put("canonicalPayload", record.canonicalPayload());
			exportedRecords.add(exportedRecord);
		}
		payload.put("records", exportedRecords);
		return payload;
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate SIF records.");
		}
		return membership;
	}

	private FiscalInvoice requireInvoice(String tenantId, String invoiceId) {
		return invoices.findByIdAndTenant_Id(invoiceId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice was not found in the tenant."));
	}

	private SifRecord requireRecord(String tenantId, String recordId) {
		return records.findByIdAndTenant_Id(recordId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("SIF record was not found in the tenant."));
	}

	private String writeJson(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Unable to render canonical SIF payload.", exception);
		}
	}

	private String hashRecord(String previousHash, String canonicalPayload) {
		return sha256(previousHash + "\n" + canonicalPayload);
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}
}
