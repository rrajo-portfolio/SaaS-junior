package com.fiscalsaas.backend.documents;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.Company;
import com.fiscalsaas.backend.identity.CompanyRepository;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.TenantAccessDeniedException;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.security.CurrentUser;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

	private static final Set<FiscalRole> WRITE_ROLES = EnumSet.of(
			FiscalRole.PLATFORM_ADMIN,
			FiscalRole.TENANT_ADMIN,
			FiscalRole.FISCAL_MANAGER,
			FiscalRole.ACCOUNTANT);

	private final TenantAccessService tenantAccess;
	private final CompanyRepository companies;
	private final FiscalDocumentRepository documents;
	private final DocumentVersionRepository versions;
	private final DocumentAuditEventRepository events;
	private final DocumentStorageService storage;

	DocumentService(
			TenantAccessService tenantAccess,
			CompanyRepository companies,
			FiscalDocumentRepository documents,
			DocumentVersionRepository versions,
			DocumentAuditEventRepository events,
			DocumentStorageService storage) {
		this.tenantAccess = tenantAccess;
		this.companies = companies;
		this.documents = documents;
		this.versions = versions;
		this.events = events;
		this.storage = storage;
	}

	@Transactional(readOnly = true)
	public List<DocumentResponse> listDocuments(String tenantId, String companyId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		String normalizedCompanyId = normalizeText(companyId);
		if (normalizedCompanyId != null) {
			requireCompany(tenantId, normalizedCompanyId);
		}
		return (normalizedCompanyId == null
				? documents.findByTenant_IdOrderByUpdatedAtDesc(tenantId)
				: documents.findByTenant_IdAndCompany_IdOrderByUpdatedAtDesc(tenantId, normalizedCompanyId))
				.stream()
				.map(document -> DocumentResponse.from(document, requireLatestVersion(document)))
				.toList();
	}

	@Transactional
	public DocumentResponse uploadDocument(
			String tenantId,
			String companyId,
			String documentType,
			String title,
			MultipartFile file,
			HttpServletRequest request) {
		Membership membership = requireWriteAccess(tenantId, request);
		CurrentUser user = tenantAccess.currentUser();
		Company company = requireCompany(tenantId, companyId);
		DocumentType parsedType = DocumentType.fromValue(documentType);
		FiscalDocument document = FiscalDocument.create(membership.tenant(), company, parsedType.name(), requireTitle(title), user.id());
		documents.save(document);
		DocumentVersion version = storeVersion(document, file, user.id(), "DOCUMENT_UPLOADED");
		return DocumentResponse.from(document, version);
	}

	@Transactional
	public DocumentResponse uploadVersion(String tenantId, String documentId, MultipartFile file, HttpServletRequest request) {
		requireWriteAccess(tenantId, request);
		FiscalDocument document = requireDocument(tenantId, documentId);
		document.addVersion();
		DocumentVersion version = storeVersion(document, file, tenantAccess.currentUser().id(), "DOCUMENT_VERSION_UPLOADED");
		return DocumentResponse.from(document, version);
	}

	@Transactional(readOnly = true)
	public DocumentDownload download(String tenantId, String documentId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		FiscalDocument document = requireDocument(tenantId, documentId);
		DocumentVersion latestVersion = requireLatestVersion(document);
		return new DocumentDownload(storage.load(latestVersion.storageKey()), latestVersion);
	}

	@Transactional(readOnly = true)
	public List<DocumentEventResponse> events(String tenantId, String documentId, HttpServletRequest request) {
		tenantAccess.requireTenantAccess(tenantId, request);
		FiscalDocument document = requireDocument(tenantId, documentId);
		return events.findByDocument_IdOrderByEventAtDesc(document.id())
				.stream()
				.map(DocumentEventResponse::from)
				.toList();
	}

	private DocumentVersion storeVersion(FiscalDocument document, MultipartFile file, String userId, String eventType) {
		StoredDocument stored = storage.store(document.tenantId(), document.id(), document.currentVersion(), file);
		DocumentVersion version = versions.save(DocumentVersion.create(
				document,
				document.currentVersion(),
				stored.originalFilename(),
				stored.contentType(),
				stored.byteSize(),
				stored.sha256(),
				stored.storageKey(),
				userId));
		events.save(DocumentAuditEvent.create(
				document.tenant(),
				document,
				eventType,
				userId,
				"sha256=" + stored.sha256() + "; bytes=" + stored.byteSize()));
		return version;
	}

	private Membership requireWriteAccess(String tenantId, HttpServletRequest request) {
		Membership membership = tenantAccess.requireTenantAccess(tenantId, request);
		if (!WRITE_ROLES.contains(membership.fiscalRole())) {
			throw new TenantAccessDeniedException("The current role cannot mutate tenant documents.");
		}
		return membership;
	}

	private Company requireCompany(String tenantId, String companyId) {
		return companies.findByIdAndTenantId(companyId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Company was not found in the tenant."));
	}

	private FiscalDocument requireDocument(String tenantId, String documentId) {
		return documents.findByIdAndTenant_Id(documentId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Document was not found in the tenant."));
	}

	private DocumentVersion requireLatestVersion(FiscalDocument document) {
		return versions.findFirstByDocument_IdOrderByVersionNumberDesc(document.id())
				.orElseThrow(() -> new ResourceNotFoundException("Document version was not found."));
	}

	private String requireTitle(String title) {
		if (title == null || title.isBlank()) {
			return "Documento fiscal";
		}
		return title.trim();
	}

	private String normalizeText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
