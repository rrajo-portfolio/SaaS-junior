package com.fiscalsaas.backend.documents;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, String> {
	Optional<DocumentVersion> findFirstByDocument_IdOrderByVersionNumberDesc(String documentId);
}
