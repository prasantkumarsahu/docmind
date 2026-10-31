package io.github.prasantkumarsahu.docmind.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import io.github.prasantkumarsahu.docmind.document.IngestionService;

@Component
public class PromptBuilder {

	static final String NO_CONTEXT_ANSWER = "I couldn't find anything about that in your documents.";

	private static final int SNIPPET_LENGTH = 220;

	private static final String SYSTEM_TEMPLATE = """
			You are DocMind, an assistant that answers questions using only the user's documents.

			Rules:
			- Use only the numbered context passages below. Do not use outside knowledge.
			- Cite the passages you used with their numbers in square brackets, for example [1] or [2][3].
			- If the context does not contain the answer, reply exactly: "%s"
			- Keep answers clear and concise. Use bullet points for lists.

			Context:
			%s
			""";

	public String systemPrompt(List<Document> documents) {
		var context = new StringBuilder();
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			context.append('[')
				.append(i + 1)
				.append("] (")
				.append(label(document.getMetadata()))
				.append(")\n")
				.append(document.getText().strip())
				.append("\n\n");
		}
		return SYSTEM_TEMPLATE.formatted(NO_CONTEXT_ANSWER, context.toString().strip());
	}

	public List<Source> sources(List<Document> documents) {
		List<Source> sources = new ArrayList<>();
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			Map<String, Object> metadata = document.getMetadata();
			sources.add(new Source(i + 1, (String) metadata.get(IngestionService.DOCUMENT_ID),
					(String) metadata.get(IngestionService.FILENAME), page(metadata), snippet(document.getText()),
					document.getScore()));
		}
		return sources;
	}

	private String label(Map<String, Object> metadata) {
		Integer page = page(metadata);
		String filename = String.valueOf(metadata.getOrDefault(IngestionService.FILENAME, "document"));
		return page == null ? filename : filename + ", page " + page;
	}

	private Integer page(Map<String, Object> metadata) {
		Object page = metadata.get("page_number");
		return page instanceof Number number ? number.intValue() : null;
	}

	private String snippet(String text) {
		String clean = text.replaceAll("\\s+", " ").strip();
		return clean.length() <= SNIPPET_LENGTH ? clean : clean.substring(0, SNIPPET_LENGTH) + "...";
	}

}
