package io.github.prasantkumarsahu.docmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

	@Bean
	ChatMemory chatMemory(ChatMemoryRepository repository, DocmindProperties properties) {
		return MessageWindowChatMemory.builder()
			.chatMemoryRepository(repository)
			.maxMessages(properties.memory().maxMessages())
			.build();
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
		return builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
	}

	@Bean
	TokenTextSplitter tokenTextSplitter(DocmindProperties properties) {
		return TokenTextSplitter.builder()
			.withChunkSize(properties.chunking().chunkSize())
			.withMinChunkSizeChars(200)
			.withMinChunkLengthToEmbed(20)
			.withKeepSeparator(true)
			.build();
	}

}
