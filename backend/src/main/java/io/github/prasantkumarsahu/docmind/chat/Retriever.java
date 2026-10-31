package io.github.prasantkumarsahu.docmind.chat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import io.github.prasantkumarsahu.docmind.config.DocmindProperties;
import io.github.prasantkumarsahu.docmind.document.IngestionService;

@Component
public class Retriever {

	private final VectorStore vectorStore;

	private final DocmindProperties properties;

	public Retriever(VectorStore vectorStore, DocmindProperties properties) {
		this.vectorStore = vectorStore;
		this.properties = properties;
	}

	public List<Document> retrieve(String question, List<UUID> documentIds) {
		var request = SearchRequest.builder()
			.query(question)
			.topK(properties.retrieval().topK())
			.similarityThreshold(properties.retrieval().similarityThreshold());
		if (!documentIds.isEmpty()) {
			request.filterExpression(documentFilter(documentIds));
		}
		return vectorStore.similaritySearch(request.build());
	}

	static String documentFilter(List<UUID> documentIds) {
		String ids = documentIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(", "));
		return IngestionService.DOCUMENT_ID + " in [" + ids + "]";
	}

}
