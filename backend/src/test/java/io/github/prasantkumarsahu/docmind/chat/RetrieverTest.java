package io.github.prasantkumarsahu.docmind.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RetrieverTest {

	@Test
	void buildsInFilterForSelectedDocuments() {
		UUID first = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID second = UUID.fromString("22222222-2222-2222-2222-222222222222");

		String filter = Retriever.documentFilter(List.of(first, second));

		assertThat(filter).isEqualTo(
				"document_id in ['11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222']");
	}

}
