package io.github.prasantkumarsahu.docmind.document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

	private final JdbcClient jdbc;

	public DocumentRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public void insert(StoredDocument document) {
		jdbc.sql("""
				INSERT INTO documents (id, filename, content_type, size_bytes, chunk_count, status, created_at)
				VALUES (:id, :filename, :contentType, :sizeBytes, :chunkCount, :status, :createdAt)
				""")
			.param("id", document.id())
			.param("filename", document.filename())
			.param("contentType", document.contentType())
			.param("sizeBytes", document.sizeBytes())
			.param("chunkCount", document.chunkCount())
			.param("status", document.status().name())
			.param("createdAt", document.createdAt())
			.update();
	}

	public void markReady(UUID id, int chunkCount) {
		jdbc.sql("UPDATE documents SET status = 'READY', chunk_count = :chunkCount, error = NULL WHERE id = :id")
			.param("id", id)
			.param("chunkCount", chunkCount)
			.update();
	}

	public void markFailed(UUID id, String error) {
		jdbc.sql("UPDATE documents SET status = 'FAILED', error = :error WHERE id = :id")
			.param("id", id)
			.param("error", error)
			.update();
	}

	public List<StoredDocument> findAll() {
		return jdbc.sql("SELECT * FROM documents ORDER BY created_at DESC").query(this::map).list();
	}

	public Optional<StoredDocument> findById(UUID id) {
		return jdbc.sql("SELECT * FROM documents WHERE id = :id").param("id", id).query(this::map).optional();
	}

	public boolean deleteById(UUID id) {
		return jdbc.sql("DELETE FROM documents WHERE id = :id").param("id", id).update() > 0;
	}

	private StoredDocument map(ResultSet rs, int rowNum) throws SQLException {
		return new StoredDocument(rs.getObject("id", UUID.class), rs.getString("filename"), rs.getString("content_type"),
				rs.getLong("size_bytes"), rs.getInt("chunk_count"), DocumentStatus.valueOf(rs.getString("status")),
				rs.getString("error"), rs.getObject("created_at", OffsetDateTime.class));
	}

}
