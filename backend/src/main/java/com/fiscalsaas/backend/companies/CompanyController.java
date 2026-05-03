package com.fiscalsaas.backend.companies;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}")
public class CompanyController {

	private final CompanyService companyService;

	CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@GetMapping("/companies")
	List<CompanyResponse> companies(@PathVariable String tenantId, HttpServletRequest request) {
		return companyService.listCompanies(tenantId, request);
	}

	@GetMapping("/companies/{companyId}")
	CompanyResponse company(@PathVariable String tenantId, @PathVariable String companyId, HttpServletRequest request) {
		return companyService.getCompany(tenantId, companyId, request);
	}

	@PostMapping("/companies")
	@ResponseStatus(HttpStatus.CREATED)
	CompanyResponse createCompany(
			@PathVariable String tenantId,
			@Valid @RequestBody CreateCompanyRequest body,
			HttpServletRequest request) {
		return companyService.createCompany(tenantId, body, request);
	}

	@PatchMapping("/companies/{companyId}")
	CompanyResponse updateCompany(
			@PathVariable String tenantId,
			@PathVariable String companyId,
			@Valid @RequestBody UpdateCompanyRequest body,
			HttpServletRequest request) {
		return companyService.updateCompany(tenantId, companyId, body, request);
	}

	@DeleteMapping("/companies/{companyId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deactivateCompany(@PathVariable String tenantId, @PathVariable String companyId, HttpServletRequest request) {
		companyService.deactivateCompany(tenantId, companyId, request);
	}

	@GetMapping("/business-relationships")
	List<BusinessRelationshipResponse> relationships(@PathVariable String tenantId, HttpServletRequest request) {
		return companyService.listRelationships(tenantId, request);
	}

	@PostMapping("/business-relationships")
	@ResponseStatus(HttpStatus.CREATED)
	BusinessRelationshipResponse createRelationship(
			@PathVariable String tenantId,
			@Valid @RequestBody CreateBusinessRelationshipRequest body,
			HttpServletRequest request) {
		return companyService.createRelationship(tenantId, body, request);
	}

	@PatchMapping("/business-relationships/{relationshipId}")
	BusinessRelationshipResponse updateRelationship(
			@PathVariable String tenantId,
			@PathVariable String relationshipId,
			@Valid @RequestBody UpdateBusinessRelationshipRequest body,
			HttpServletRequest request) {
		return companyService.updateRelationship(tenantId, relationshipId, body, request);
	}

	@DeleteMapping("/business-relationships/{relationshipId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deactivateRelationship(
			@PathVariable String tenantId,
			@PathVariable String relationshipId,
			HttpServletRequest request) {
		companyService.deactivateRelationship(tenantId, relationshipId, request);
	}
}
