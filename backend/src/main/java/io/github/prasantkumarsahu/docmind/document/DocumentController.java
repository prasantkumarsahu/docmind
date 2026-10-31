package io.github.prasantkumarsahu.docmind.document;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public StoredDocument upload(@RequestParam("file") MultipartFile file) throws IOException {
		return documentService.upload(file);
	}

	@GetMapping
	public List<StoredDocument> list() {
		return documentService.findAll();
	}

	@GetMapping("/{id}")
	public StoredDocument get(@PathVariable UUID id) {
		return documentService.findById(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		documentService.delete(id);
	}

}
