package com.fiscalsaas.backend.verifactu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiscalsaas.backend.api.ApiValidationException;
import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessDeniedException;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifactuComplianceService {

	private static final EnumSet<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);

	private final TenantAccessService tenantAccess;
	private final SifRecordRepository records;
	private final SifQrPayloadRepository qrPayloads;
	private final SifTransmissionAttemptRepository transmissions;
	private final SifSystemDeclarationRepository declarations;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AeatTransmissionMode defaultMode;
	private final boolean productionEnabled;

	VerifactuComplianceService(
			TenantAccessService tenantAccess,
			SifRecordRepository records,
			SifQrPayloadRepository qrPayloads,
			SifTransmissionAttemptRepository transmissions,
			SifSystemDeclarationRepository declarations,
			@Value("${app.verifactu.aeat.mode:stub}") String defaultMode,
			@Value("${app.verifactu.aeat.production-enabled:false}") boolean productionEnabled) {
		this.tenantAccess = tenantAccess;
		this.records = records;
		this.qrPayloads = qrPayloads;
		this.transmissions = transmissions;
		this.declarations = declarations;
		this.defaultMode = AeatTransmissionMode.fromValue(defaultMode, AeatTransmissionMode.STUB);
		this.productionEnabled = productionEnabled;
	}

	@Transactional
	public SifQrPayloadResponse qrPayload(String tenantId, String recordId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		SifRecord record = requireRecord(tenantId, recordId);
		return qrPayloads.findByTenant_IdAndRecord_Id(tenantId, recordId)
				.map(SifQrPayloadResponse::from)
				.orElseGet(() -> {
					String qrPayload = buildQrPayload(record);
					return SifQrPayloadResponse.from(qrPayloads.save(SifQrPayload.create(
							record,
							qrPayload,
							sha256(qrPayload),
							Instant.now())));
				});
	}

	@Transactional
	public String qrSvg(String tenantId, String recordId, HttpServletRequest request) {
		SifQrPayloadResponse payload = qrPayload(tenantId, recordId, request);
		try {
			BitMatrix matrix = new QRCodeWriter().encode(payload.qrPayload(), BarcodeFormat.QR_CODE, 192, 192);
			StringBuilder svg = new StringBuilder();
			svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
					.append(matrix.getWidth())
					.append(' ')
					.append(matrix.getHeight())
					.append("\" shape-rendering=\"crispEdges\">");
			svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>");
			for (int y = 0; y < matrix.getHeight(); y++) {
				for (int x = 0; x < matrix.getWidth(); x++) {
					if (matrix.get(x, y)) {
						svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"1\" height=\"1\" fill=\"#000000\"/>");
					}
				}
			}
			svg.append("</svg>");
			return svg.toString();
		} catch (WriterException exception) {
			throw new IllegalStateException("Unable to render SIF QR SVG.", exception);
		}
	}

	@Transactional
	public SifTransmissionAttemptResponse transmit(
			String tenantId,
			String recordId,
			CreateAeatTransmissionRequest body,
			HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		SifRecord record = requireRecord(tenantId, recordId);
		AeatTransmissionMode mode = AeatTransmissionMode.fromValue(body == null ? null : body.mode(), defaultMode);
		if (mode == AeatTransmissionMode.PRODUCTION && !productionEnabled) {
			throw new ApiValidationException("AEAT production transmission is disabled by default.");
		}
		SifTransmissionStatus status = switch (mode) {
			case STUB -> SifTransmissionStatus.STUB_ACCEPTED;
			case SANDBOX -> SifTransmissionStatus.SANDBOX_QUEUED;
			case PRODUCTION -> SifTransmissionStatus.PRODUCTION_BLOCKED;
		};
		Instant now = Instant.now();
		String requestPayload = writeJson(transmissionRequestPayload(record, mode, now));
		String responsePayload = writeJson(transmissionResponsePayload(record, mode, status, now));
		return SifTransmissionAttemptResponse.from(transmissions.save(SifTransmissionAttempt.create(
				membership.tenant(),
				record,
				mode,
				status,
				requestPayload,
				responsePayload,
				tenantAccess.currentUser().id(),
				now)));
	}

	@Transactional(readOnly = true)
	public List<SifTransmissionAttemptResponse> listTransmissions(String tenantId, String recordId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireRecord(tenantId, recordId);
		return transmissions.findByTenant_IdAndRecord_IdOrderByCreatedAtDesc(tenantId, recordId)
				.stream()
				.map(SifTransmissionAttemptResponse::from)
				.toList();
	}

	@Transactional
	public SifSystemDeclarationResponse createSystemDeclarationDraft(String tenantId, HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		Instant now = Instant.now();
		String payload = writeJson(systemDeclarationPayload(tenantId, now));
		return SifSystemDeclarationResponse.from(declarations.save(SifSystemDeclaration.create(
				membership.tenant(),
				payload,
				sha256(payload),
				tenantAccess.currentUser().id(),
				now)));
	}

	@Transactional(readOnly = true)
	public List<SifSystemDeclarationResponse> listSystemDeclarationDrafts(String tenantId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		return declarations.findByTenant_IdOrderByCreatedAtDesc(tenantId)
				.stream()
				.map(SifSystemDeclarationResponse::from)
				.toList();
	}

	private String buildQrPayload(SifRecord record) {
		return "https://verifactu.local/verify?tenant=%s&record=%s&sequence=%d&hash=%s&invoice=%s"
				.formatted(
						record.tenantId(),
						record.id(),
						record.sequenceNumber(),
						record.recordHash(),
						record.invoice().invoiceNumber());
	}

	private Map<String, Object> transmissionRequestPayload(SifRecord record, AeatTransmissionMode mode, Instant createdAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("mode", mode.name());
		payload.put("recordId", record.id());
		payload.put("recordType", record.recordType());
		payload.put("sequenceNumber", record.sequenceNumber());
		payload.put("recordHash", record.recordHash());
		payload.put("canonicalPayload", record.canonicalPayload());
		payload.put("createdAt", createdAt.toString());
		return payload;
	}

	private Map<String, Object> transmissionResponsePayload(
			SifRecord record,
			AeatTransmissionMode mode,
			SifTransmissionStatus status,
			Instant createdAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("mode", mode.name());
		payload.put("status", status.name());
		payload.put("recordId", record.id());
		payload.put("externalTransmission", false);
		payload.put("message", mode == AeatTransmissionMode.STUB ? "Stub accepted without external AEAT call" : "Queued without production dispatch");
		payload.put("createdAt", createdAt.toString());
		return payload;
	}

	private Map<String, Object> systemDeclarationPayload(String tenantId, Instant createdAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("tenantId", tenantId);
		payload.put("systemName", "Fiscal SaaS");
		payload.put("systemCode", "FISCAL-SAAS-SIF");
		payload.put("systemVersion", "0.1.0");
		payload.put("normativeReference", "CURRENT_AS_OF_2026_05_03");
		payload.put("supportsMultipleTaxpayers", true);
		payload.put("verifactuOnlyMode", false);
		payload.put("aeatMode", defaultMode.name());
		payload.put("aeatProductionEnabled", productionEnabled);
		payload.put("certified", false);
		payload.put("reviewRequired", true);
		payload.put("createdAt", createdAt.toString());
		return payload;
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate Verifactu evidence.");
		}
		return membership;
	}

	private SifRecord requireRecord(String tenantId, String recordId) {
		return records.findByIdAndTenant_Id(recordId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("SIF record was not found in the tenant."));
	}

	private String writeJson(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Unable to render Verifactu payload.", exception);
		}
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
