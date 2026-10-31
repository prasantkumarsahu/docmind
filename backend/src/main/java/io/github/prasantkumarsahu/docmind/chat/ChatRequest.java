package io.github.prasantkumarsahu.docmind.chat;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(@NotBlank @Size(max = 100) String conversationId, @NotBlank @Size(max = 2000) String question,
		List<UUID> documentIds) {

	public List<UUID> documentIds() {
		return documentIds == null ? List.of() : documentIds;
	}

}
