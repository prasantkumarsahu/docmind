package io.github.prasantkumarsahu.docmind.document;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

	private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "docx", "txt", "md", "html");

	private final DocumentRepository repository;

	private final IngestionService ingestionService;

	public DocumentService(DocumentRepository repository, IngestionService ingestionService) {
		this.repository = repository;
		this.ingestionService = ingestionService;
	}

	public StoredDocument upload(MultipartFile file) throws IOException {
		String filename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
		validate(file, filename);
		var document = new StoredDocument(UUID.randomUUID(), filename, file.getContentType(), file.getSize(), 0,
				DocumentStatus.PROCESSING, null, OffsetDateTime.now());
		repository.insert(document);
		ingestionService.ingest(document.id(), filename, file.getBytes());
		return document;
	}

	public List<StoredDocument> findAll() {
		return repository.findAll();
	}

	public StoredDocument findById(UUID id) {
		return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
	}

	public void delete(UUID id) {
		findById(id);
		ingestionService.removeChunks(id);
		repository.deleteById(id);
	}

	private void validate(MultipartFile file, String filename) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty");
		}
		String extension = StringUtils.getFilenameExtension(filename);
		if (extension == null || !SUPPORTED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException("Unsupported file type. Allowed: " + SUPPORTED_EXTENSIONS);
		}
	}

}
