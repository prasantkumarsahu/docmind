package io.github.prasantkumarsahu.docmind.document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StoredDocument(UUID id, String filename, String contentType, long sizeBytes, int chunkCount,
		DocumentStatus status, String error, OffsetDateTime createdAt) {

}
