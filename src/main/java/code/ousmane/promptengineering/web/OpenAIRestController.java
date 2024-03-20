package code.ousmane.promptengineering.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class OpenAIRestController {
    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;
    @Autowired
    private ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(String message){
        return chatClient.call(message);
    }

    @GetMapping("/movies")
    public Map movies(@RequestParam(name = "category", defaultValue = "action") String category,
                         @RequestParam(name = "year", defaultValue = "2020") int year) throws JsonProcessingException {
        OpenAiApi apiKey = new OpenAiApi(API_KEY);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-4")
                .withTemperature(0F)
                .withMaxTokens(2000)
                .build();
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(apiKey);
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(
                """
                        Hey I need you to give me the best movie on this category: {category}
                        on the given year: {year}.
                        The output should be in the JSON format including this fields:
                        - Category: <the given category>
                        - Year: <the given year>
                        - Producer: <the producer of the movie>
                        - Actors: <the main actors>
                        - Summary: <a brief summary of the movie>
                        """
        );
        Prompt prompt = systemPrompt.create(Map.of("category", category, "year", year));
        ChatResponse response = openAiChatClient.call(prompt);
        String content = response.getResult().getOutput().getContent();
        return new ObjectMapper().readValue(content, Map.class);
    }

    @GetMapping("/sentiment")
    public String sentimentAnalysis(String review){
        OpenAiApi apiKey = new OpenAiApi(API_KEY);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-4")
                .withTemperature(0F)
                .withMaxTokens(2000)
                .build();
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(apiKey);
        String systemMessageText =
                """
                Perform aspect based sentiment analysis for this review presentend in the input delimited by triple backticks 
                {review} which contains aspects: key, mouse, screen
                For each review presented as input:
                - Identify aspects present in the review 
                """;
        SystemMessage systemMessage = new SystemMessage(systemMessageText);
        UserMessage userMessage = new UserMessage("```"+review+"```");
        Prompt zeroShotPrompt = new Prompt(List.of(systemMessage, userMessage)); //zero shot prompt
        ChatResponse response = openAiChatClient.call(zeroShotPrompt);
        return response.getResult().getOutput().getContent();
    }
}
