package io.github.prasantkumarsahu.docmind.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class DocumentServiceTest {

	private final DocumentRepository repository = mock(DocumentRepository.class);

	private final IngestionService ingestionService = mock(IngestionService.class);

	private final DocumentService service = new DocumentService(repository, ingestionService);

	@Test
	void uploadStoresDocumentAndStartsIngestion() throws Exception {
		var file = new MockMultipartFile("file", "notes.md", "text/markdown", "# Notes".getBytes());

		StoredDocument document = service.upload(file);

		assertThat(document.status()).isEqualTo(DocumentStatus.PROCESSING);
		assertThat(document.filename()).isEqualTo("notes.md");
		verify(repository).insert(document);
		verify(ingestionService).ingest(eq(document.id()), eq("notes.md"), any());
	}

	@Test
	void rejectsUnsupportedFileTypes() {
		var file = new MockMultipartFile("file", "app.exe", "application/octet-stream", new byte[] { 1 });

		assertThatThrownBy(() -> service.upload(file)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unsupported file type");
		verify(repository, never()).insert(any());
	}

	@Test
	void rejectsEmptyFiles() {
		var file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

		assertThatThrownBy(() -> service.upload(file)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("File is empty");
	}

	@Test
	void deleteRemovesChunksAndRecord() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.of(new StoredDocument(id, "a.pdf", "application/pdf", 10, 3,
				DocumentStatus.READY, null, null)));

		service.delete(id);

		verify(ingestionService).removeChunks(id);
		verify(repository).deleteById(id);
	}

	@Test
	void deleteUnknownDocumentThrows() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(id)).isInstanceOf(DocumentNotFoundException.class);
	}

}
