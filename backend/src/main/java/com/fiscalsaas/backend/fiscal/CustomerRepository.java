package com.fiscalsaas.backend.fiscal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, String> {
	@Query("""
			select customer
			from Customer customer
			where customer.tenant.id = :tenantId
				and customer.company.id = :companyId
				and (:status is null or customer.status = :status)
				and (
					:search is null
					or lower(customer.name) like lower(concat('%', :search, '%'))
					or lower(customer.nif) like lower(concat('%', :search, '%'))
					or lower(customer.email) like lower(concat('%', :search, '%'))
				)
			order by customer.name asc
			""")
	List<Customer> search(
			@Param("tenantId") String tenantId,
			@Param("companyId") String companyId,
			@Param("search") String search,
			@Param("status") String status);

	Optional<Customer> findByIdAndTenant_IdAndCompany_Id(String id, String tenantId, String companyId);

	Optional<Customer> findByIdAndTenant_Id(String id, String tenantId);

	boolean existsByCompany_IdAndTenant_IdAndNif(String companyId, String tenantId, String nif);

	boolean existsByCompany_IdAndTenant_IdAndNifAndIdNot(String companyId, String tenantId, String nif, String id);
}
