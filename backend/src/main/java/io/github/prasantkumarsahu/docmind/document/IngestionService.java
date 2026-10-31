package io.github.prasantkumarsahu.docmind.document;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

	public static final String DOCUMENT_ID = "document_id";

	public static final String FILENAME = "filename";

	private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

	private final DocumentParser parser;

	private final TokenTextSplitter splitter;

	private final VectorStore vectorStore;

	private final DocumentRepository repository;

	public IngestionService(DocumentParser parser, TokenTextSplitter splitter, VectorStore vectorStore,
			DocumentRepository repository) {
		this.parser = parser;
		this.splitter = splitter;
		this.vectorStore = vectorStore;
		this.repository = repository;
	}

	@Async
	public void ingest(UUID documentId, String filename, byte[] content) {
		try {
			var resource = new ByteArrayResource(content) {
				@Override
				public String getFilename() {
					return filename;
				}
			};
			List<Document> pages = parser.parse(resource, filename);
			List<Document> chunks = splitter.apply(pages);
			chunks.forEach(chunk -> {
				chunk.getMetadata().put(DOCUMENT_ID, documentId.toString());
				chunk.getMetadata().put(FILENAME, filename);
			});
			if (chunks.isEmpty()) {
				repository.markFailed(documentId, "No readable text found in the file");
				return;
			}
			vectorStore.add(chunks);
			repository.markReady(documentId, chunks.size());
			log.info("Ingested {} ({} chunks)", filename, chunks.size());
		}
		catch (Exception ex) {
			log.error("Failed to ingest {}", filename, ex);
			repository.markFailed(documentId, ex.getMessage());
		}
	}

	public void removeChunks(UUID documentId) {
		vectorStore.delete(DOCUMENT_ID + " == '" + documentId + "'");
	}

}
