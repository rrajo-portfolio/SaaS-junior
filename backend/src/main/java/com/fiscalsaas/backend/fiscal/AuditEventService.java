package com.fiscalsaas.backend.fiscal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.AppUser;
import com.fiscalsaas.backend.identity.AppUserRepository;
import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.CompanyRepository;
import com.fiscalsaas.backend.identity.Tenant;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.identity.TenantRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventService {

	private static final String GENESIS_HASH = "GENESIS";

	private final TenantAccessService tenantAccess;
	private final TenantRepository tenants;
	private final CompanyRepository companies;
	private final AppUserRepository users;
	private final AuditEventRepository events;

	AuditEventService(
			TenantAccessService tenantAccess,
			TenantRepository tenants,
			CompanyRepository companies,
			AppUserRepository users,
			AuditEventRepository events) {
		this.tenantAccess = tenantAccess;
		this.tenants = tenants;
		this.companies = companies;
		this.users = users;
		this.events = events;
	}

	@Transactional(readOnly = true)
	public List<AuditEventResponse> listCompanyEvents(String tenantId, String companyId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		requireCompany(tenantId, companyId);
		return events.findByTenant_IdAndCompany_IdOrderByOccurredAtDesc(tenantId, companyId)
				.stream()
				.map(AuditEventResponse::from)
				.toList();
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public AuditEvent record(String tenantId, String companyId, String eventType, String entityType, String entityId, String details) {
		Tenant tenant = tenants.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant was not found."));
		Company company = companyId == null ? null : requireCompany(tenantId, companyId);
		AppUser actor = users.findById(tenantAccess.currentUser().id())
				.orElseThrow(() -> new ResourceNotFoundException("Actor was not found."));
		Instant occurredAt = Instant.now();
		String previousHash = companyId == null
				? events.findFirstByTenant_IdOrderByOccurredAtDesc(tenantId).map(AuditEvent::eventHash).orElse(GENESIS_HASH)
				: events.findLastForCompanyForUpdate(tenantId, companyId, PageRequest.of(0, 1))
						.stream()
						.findFirst()
						.map(AuditEvent::eventHash)
						.orElse(GENESIS_HASH);
		String canonical = tenantId + "|" + companyId + "|" + eventType + "|" + entityType + "|" + entityId + "|"
				+ details + "|" + previousHash + "|" + occurredAt;
		String hash = sha256(canonical);
		return events.save(AuditEvent.create(
				tenant,
				company,
				actor,
				eventType,
				entityType,
				entityId,
				details,
				previousHash,
				hash,
				occurredAt));
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
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
