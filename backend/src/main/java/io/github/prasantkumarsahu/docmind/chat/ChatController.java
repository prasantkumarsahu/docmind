package io.github.prasantkumarsahu.docmind.chat;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	private static final Duration STREAM_TIMEOUT = Duration.ofMinutes(5);

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping
	public ChatAnswer ask(@Valid @RequestBody ChatRequest request) {
		return chatService.ask(request);
	}

	@PostMapping("/stream")
	public SseEmitter stream(@Valid @RequestBody ChatRequest request) {
		var emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
		var answer = chatService.stream(request);
		send(emitter, "sources", answer.sources());
		answer.tokens()
			.subscribe(token -> send(emitter, "token", Map.of("text", token)), error -> {
				log.error("Streaming failed", error);
				send(emitter, "error", Map.of("message", "The model failed to answer. Is Ollama running?"));
				emitter.complete();
			}, () -> {
				send(emitter, "done", Map.of());
				emitter.complete();
			});
		return emitter;
	}

	@DeleteMapping("/{conversationId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void clear(@PathVariable String conversationId) {
		chatService.clearConversation(conversationId);
	}

	private void send(SseEmitter emitter, String event, Object data) {
		try {
			emitter.send(SseEmitter.event().name(event).data(data));
		}
		catch (IOException | IllegalStateException ex) {
			log.debug("Client disconnected during stream", ex);
		}
	}

}
