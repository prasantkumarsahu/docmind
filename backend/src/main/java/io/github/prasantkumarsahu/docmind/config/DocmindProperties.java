package io.github.prasantkumarsahu.docmind.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "docmind")
public record DocmindProperties(Retrieval retrieval, Chunking chunking, Memory memory, Cors cors) {

	public record Retrieval(int topK, double similarityThreshold) {
	}

	public record Chunking(int chunkSize) {
	}

	public record Memory(int maxMessages) {
	}

	public record Cors(List<String> allowedOrigins) {
	}

}
