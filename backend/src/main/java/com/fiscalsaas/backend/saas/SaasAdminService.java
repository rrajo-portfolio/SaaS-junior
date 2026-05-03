package com.fiscalsaas.backend.saas;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import com.fiscalsaas.backend.api.ApiConflictException;
import com.fiscalsaas.backend.api.ApiValidationException;
import com.fiscalsaas.backend.api.ResourceNotFoundException;
import com.fiscalsaas.backend.identity.AppUser;
import com.fiscalsaas.backend.identity.AppUserRepository;
import com.fiscalsaas.backend.identity.FiscalRole;
import com.fiscalsaas.backend.identity.Membership;
import com.fiscalsaas.backend.identity.MembershipRepository;
import com.fiscalsaas.backend.identity.Tenant;
import com.fiscalsaas.backend.identity.TenantAccessService;
import com.fiscalsaas.backend.identity.TenantRepository;
import com.fiscalsaas.backend.security.CurrentUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaasAdminService {

	private final TenantAccessService tenantAccess;
	private final TenantRepository tenants;
	private final AppUserRepository users;
	private final MembershipRepository memberships;
	private final SubscriptionPlanRepository plans;
	private final TenantLifecycleEventRepository lifecycleEvents;

	SaasAdminService(
			TenantAccessService tenantAccess,
			TenantRepository tenants,
			AppUserRepository users,
			MembershipRepository memberships,
			SubscriptionPlanRepository plans,
			TenantLifecycleEventRepository lifecycleEvents) {
		this.tenantAccess = tenantAccess;
		this.tenants = tenants;
		this.users = users;
		this.memberships = memberships;
		this.plans = plans;
		this.lifecycleEvents = lifecycleEvents;
	}

	@Transactional(readOnly = true)
	public List<SubscriptionPlanResponse> listPlans() {
		tenantAccess.currentUser();
		return plans.findByStatusOrderByMonthlyPriceCentsAsc("ACTIVE")
				.stream()
				.map(SubscriptionPlanResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TenantAdminResponse> listTenants() {
		tenantAccess.requirePlatformAdmin();
		return tenants.findAll()
				.stream()
				.sorted((left, right) -> left.displayName().compareToIgnoreCase(right.displayName()))
				.map(TenantAdminResponse::from)
				.toList();
	}

	@Transactional
	public TenantAdminResponse createTenant(CreateTenantRequest request) {
		CurrentUser actor = tenantAccess.requirePlatformAdmin();
		String slug = request.slug().trim().toLowerCase(Locale.ROOT);
		String planCode = request.planCode().trim().toLowerCase(Locale.ROOT);
		if (tenants.existsBySlug(slug)) {
			throw new ApiConflictException("A tenant with this slug already exists.");
		}
		SubscriptionPlan plan = requirePlan(planCode);
		String adminEmail = request.adminEmail().trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmailIgnoreCase(adminEmail)) {
			throw new ApiConflictException("An application user with this admin email already exists.");
		}

		Tenant tenant = Tenant.create(slug, request.displayName(), plan.code(), Instant.now().plus(14, ChronoUnit.DAYS));
		AppUser admin = AppUser.create(adminEmail, request.adminDisplayName());
		tenants.save(tenant);
		users.save(admin);
		memberships.save(Membership.create(tenant, admin, FiscalRole.TENANT_ADMIN));
		lifecycleEvents.save(TenantLifecycleEvent.create(
				tenant,
				"tenant_created",
				requireActor(actor),
				null,
				tenant.status(),
				null,
				tenant.planCode(),
				"Tenant created with trial access."));
		return TenantAdminResponse.from(tenant);
	}

	@Transactional
	public TenantAdminResponse updateStatus(String tenantId, UpdateTenantStatusRequest request) {
		CurrentUser actor = tenantAccess.requirePlatformAdmin();
		Tenant tenant = requireTenant(tenantId);
		String previousStatus = tenant.status();
		String requestedStatus = request.status().trim().toLowerCase(Locale.ROOT);
		switch (requestedStatus) {
			case "active" -> tenant.activate();
			case "suspended" -> tenant.suspend();
			case "cancelled" -> tenant.cancel();
			default -> throw new ApiValidationException("Unsupported tenant status: " + request.status());
		}
		lifecycleEvents.save(TenantLifecycleEvent.create(
				tenant,
				"tenant_status_changed",
				requireActor(actor),
				previousStatus,
				tenant.status(),
				tenant.planCode(),
				tenant.planCode(),
				request.notes()));
		return TenantAdminResponse.from(tenant);
	}

	@Transactional
	public TenantAdminResponse changePlan(String tenantId, ChangeTenantPlanRequest request) {
		CurrentUser actor = tenantAccess.requirePlatformAdmin();
		Tenant tenant = requireTenant(tenantId);
		String previousPlan = tenant.planCode();
		SubscriptionPlan plan = requirePlan(request.planCode().trim().toLowerCase(Locale.ROOT));
		tenant.changePlan(plan.code());
		lifecycleEvents.save(TenantLifecycleEvent.create(
				tenant,
				"tenant_plan_changed",
				requireActor(actor),
				tenant.status(),
				tenant.status(),
				previousPlan,
				tenant.planCode(),
				request.notes()));
		return TenantAdminResponse.from(tenant);
	}

	@Transactional(readOnly = true)
	public List<TenantLifecycleEventResponse> listEvents(String tenantId) {
		tenantAccess.requirePlatformAdmin();
		requireTenant(tenantId);
		return lifecycleEvents.findByTenantIdOrderByCreatedAtDesc(tenantId)
				.stream()
				.map(TenantLifecycleEventResponse::from)
				.toList();
	}

	private SubscriptionPlan requirePlan(String planCode) {
		return plans.findById(planCode)
				.filter(plan -> "ACTIVE".equals(plan.status()))
				.orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found."));
	}

	private Tenant requireTenant(String tenantId) {
		return tenants.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant not found."));
	}

	private AppUser requireActor(CurrentUser actor) {
		return users.findById(actor.id())
				.orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
	}
}
