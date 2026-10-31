package io.github.prasantkumarsahu.docmind.document;

import java.util.List;
import java.util.Locale;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class DocumentParser {

	public List<Document> parse(Resource resource, String filename) {
		if (isPdf(filename)) {
			return new PagePdfDocumentReader(resource).get();
		}
		return new TikaDocumentReader(resource).get();
	}

	private boolean isPdf(String filename) {
		return filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
	}

}
