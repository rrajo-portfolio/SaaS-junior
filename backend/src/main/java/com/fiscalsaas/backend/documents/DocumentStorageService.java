package com.fiscalsaas.backend.documents;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.fiscalsaas.backend.api.ApiValidationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {

	private final Path root;
	private final long maxBytes;
	private final Set<String> allowedContentTypes = Set.of(
			"application/pdf",
			"application/xml",
			"text/xml",
			"text/plain",
			"image/png",
			"image/jpeg");

	DocumentStorageService(
			@Value("${app.documents.storage-path}") String storagePath,
			@Value("${app.documents.max-bytes:26214400}") long maxBytes) {
		this.root = Path.of(storagePath).toAbsolutePath().normalize();
		this.maxBytes = maxBytes;
	}

	public StoredDocument store(String tenantId, String documentId, int versionNumber, MultipartFile file) {
		if (file.isEmpty()) {
			throw new ApiValidationException("file is required.");
		}
		if (file.getSize() > maxBytes) {
			throw new ApiValidationException("file exceeds the configured maximum size.");
		}
		try {
			Files.createDirectories(root);
			String originalFilename = sanitizeFilename(file.getOriginalFilename());
			String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
			validateContentTypeAndExtension(contentType, originalFilename);
			String storageKey = tenantId + "/" + documentId + "/v" + versionNumber + "-" + UUID.randomUUID();
			Path target = resolveStorageKey(storageKey);
			Files.createDirectories(target.getParent());

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			long bytes;
			try (InputStream source = new DigestInputStream(file.getInputStream(), digest)) {
				bytes = Files.copy(source, target);
			}
			return new StoredDocument(storageKey, HexFormat.of().formatHex(digest.digest()), bytes, contentType, originalFilename);
		} catch (IOException | NoSuchAlgorithmException exception) {
			throw new ApiValidationException("Document storage failed.");
		}
	}

	public Resource load(String storageKey) {
		try {
			Path path = resolveStorageKey(storageKey);
			Resource resource = new UrlResource(path.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new ApiValidationException("Document binary is not readable.");
			}
			return resource;
		} catch (IOException exception) {
			throw new ApiValidationException("Document binary is not readable.");
		}
	}

	private Path resolveStorageKey(String storageKey) {
		Path resolved = root.resolve(storageKey).normalize();
		if (!resolved.startsWith(root)) {
			throw new ApiValidationException("Invalid document storage key.");
		}
		return resolved;
	}

	private String sanitizeFilename(String filename) {
		if (filename == null || filename.isBlank()) {
			return "document.bin";
		}
		return filename.replaceAll("[\\\\/\\r\\n]", "_").trim();
	}

	private void validateContentTypeAndExtension(String contentType, String filename) {
		String normalizedType = contentType.toLowerCase(Locale.ROOT);
		String normalizedName = filename.toLowerCase(Locale.ROOT);
		boolean extensionAllowed = normalizedName.endsWith(".pdf")
				|| normalizedName.endsWith(".xml")
				|| normalizedName.endsWith(".txt")
				|| normalizedName.endsWith(".png")
				|| normalizedName.endsWith(".jpg")
				|| normalizedName.endsWith(".jpeg");
		if (!allowedContentTypes.contains(normalizedType) || !extensionAllowed) {
			throw new ApiValidationException("Unsupported document type.");
		}
	}
}
