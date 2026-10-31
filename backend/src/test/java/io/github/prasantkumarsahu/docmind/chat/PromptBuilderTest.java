package io.github.prasantkumarsahu.docmind.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

class PromptBuilderTest {

	private final PromptBuilder promptBuilder = new PromptBuilder();

	private final List<Document> documents = List.of(
			Document.builder()
				.text("Employees get 24 days of paid leave per year.")
				.metadata(Map.of("document_id", "doc-1", "filename", "handbook.pdf", "page_number", 4))
				.score(0.82)
				.build(),
			Document.builder()
				.text("Remote work is allowed   two days\na week.")
				.metadata(Map.of("document_id", "doc-2", "filename", "policy.md"))
				.score(0.61)
				.build());

	@Test
	void systemPromptNumbersEachPassageWithItsSource() {
		String prompt = promptBuilder.systemPrompt(documents);

		assertThat(prompt).contains("[1] (handbook.pdf, page 4)\nEmployees get 24 days of paid leave per year.")
			.contains("[2] (policy.md)\nRemote work is allowed")
			.contains(PromptBuilder.NO_CONTEXT_ANSWER);
	}

	@Test
	void sourcesKeepOrderMetadataAndNormalisedSnippets() {
		List<Source> sources = promptBuilder.sources(documents);

		assertThat(sources).hasSize(2);
		assertThat(sources.get(0)).isEqualTo(new Source(1, "doc-1", "handbook.pdf", 4,
				"Employees get 24 days of paid leave per year.", 0.82));
		assertThat(sources.get(1).page()).isNull();
		assertThat(sources.get(1).snippet()).isEqualTo("Remote work is allowed two days a week.");
	}

	@Test
	void longSnippetsAreTruncated() {
		var longDocument = Document.builder().text("word ".repeat(200)).metadata(Map.of("filename", "long.txt")).build();

		String snippet = promptBuilder.sources(List.of(longDocument)).get(0).snippet();

		assertThat(snippet).hasSize(223).endsWith("...");
	}

}
