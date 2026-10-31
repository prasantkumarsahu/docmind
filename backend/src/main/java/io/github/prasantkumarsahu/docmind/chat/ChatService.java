package io.github.prasantkumarsahu.docmind.chat;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

	private final ChatClient chatClient;

	private final ChatMemory chatMemory;

	private final Retriever retriever;

	private final PromptBuilder promptBuilder;

	public ChatService(ChatClient chatClient, ChatMemory chatMemory, Retriever retriever, PromptBuilder promptBuilder) {
		this.chatClient = chatClient;
		this.chatMemory = chatMemory;
		this.retriever = retriever;
		this.promptBuilder = promptBuilder;
	}

	public ChatAnswer ask(ChatRequest request) {
		List<Document> documents = retriever.retrieve(request.question(), request.documentIds());
		if (documents.isEmpty()) {
			return new ChatAnswer(PromptBuilder.NO_CONTEXT_ANSWER, List.of());
		}
		String answer = prompt(request, documents).call().content();
		return new ChatAnswer(answer, promptBuilder.sources(documents));
	}

	public StreamingAnswer stream(ChatRequest request) {
		List<Document> documents = retriever.retrieve(request.question(), request.documentIds());
		if (documents.isEmpty()) {
			return new StreamingAnswer(List.of(), Flux.just(PromptBuilder.NO_CONTEXT_ANSWER));
		}
		return new StreamingAnswer(promptBuilder.sources(documents), prompt(request, documents).stream().content());
	}

	public void clearConversation(String conversationId) {
		chatMemory.clear(conversationId);
	}

	private ChatClient.ChatClientRequestSpec prompt(ChatRequest request, List<Document> documents) {
		return chatClient.prompt()
			.system(promptBuilder.systemPrompt(documents))
			.user(request.question())
			.advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()));
	}

	public record StreamingAnswer(List<Source> sources, Flux<String> tokens) {
	}

}
