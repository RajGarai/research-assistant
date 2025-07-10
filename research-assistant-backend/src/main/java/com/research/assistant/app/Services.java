package com.research.assistant.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

@Service
public class Services {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> TEMPLATES;

    static {
        Map<String, String> m = Map.ofEntries(
                // Text Editing
                Map.entry("rewrite", "Rewrite the following text to improve clarity and grammar in a simple way:\n\n"),
                Map.entry("expand", "Expand the following text into a more detailed explanation or article:\n\n"),
                Map.entry("shorten", "Shorten the following text while keeping the core message intact:\n\n"),
                Map.entry("tone_change", "Change the tone of the following text as specified:\n\n"),

                // Text Analysis
                Map.entry("summarize", "Provide a clear and concise summary of the following text in a few sentences:\n\n"),
                Map.entry("summarize_bullets", "Summarize the following content into concise bullet points:\n\n"),
                Map.entry("extract_keywords", "Extract important keywords from the following text:\n\n"),
                Map.entry("generate_outline", "Create a structured outline for an article based on the following topic or content:\n\n"),
                Map.entry("generate_title", "Generate a compelling title for the following content:\n\n"),
                Map.entry("question", "Generate insightful questions based on the following text:\n\n"),

                // Grammar Tools
                Map.entry("check_grammar", "Correct any grammatical mistakes in the following text:\n\n"),
                Map.entry("synonyms", "Provide synonyms for key words and phrases in the following text:\n\n"),
                Map.entry("antonyms", "Provide antonyms for key words and phrases in the following text:\n\n"),
                Map.entry("paraphrase", "Paraphrase the following text while preserving its original meaning:\n\n"),
                Map.entry("correct_punctuation", "Correct the punctuation in the following text:\n\n"),
                Map.entry("simplify_text", "Simplify the following text to make it more accessible:\n\n"),
                Map.entry("expand_phrases", "Expand the following phrases into full sentences or explanations:\n\n"),
                Map.entry("verb_conjugation", "Provide verb conjugations for verbs in the following text:\n\n"),

                // Technical
                Map.entry("suggest", "Based on the following content, suggest related topics and further reading. Format with headings and bullet points:\n\n"),
                Map.entry("code_explain", "Explain the following code snippet in simple terms:\n\n"),
                Map.entry("quary_explain", "Explain the following database query in plain language:\n\n"),
                Map.entry("code_comment", "Add clear comments to the following code snippet:\n\n"),
                Map.entry("code_debug", "Identify and fix errors in the following code snippet:\n\n"),
                Map.entry("convert_to_pseudocode", "Convert the following code into readable pseudocode:\n\n"),
                Map.entry("complexity_analysis", "Analyze the time and space complexity of the following algorithm:\n\n"),
                Map.entry("generate_test_cases", "Generate test cases for the following function or code snippet:\n\n"),
                Map.entry("real_application", "Describe real-world applications of the following concept or code:\n\n"),

                // Translation
                Map.entry("translate_bengali", "Translate the following text into Bengali:\n\n"),
                Map.entry("translate_hindi", "Translate the following text into Hindi:\n\n"),
                Map.entry("translate_english", "Translate the following text into English:\n\n"),
                Map.entry("translate_other", "Translate the following text into the specified target language:\n\n"),

                // AI Insights
                Map.entry("sentiment_analysis", "Analyze the sentiment of the following text (positive, negative, or neutral):\n\n"),
                Map.entry("readability_score", "Calculate the readability score (e.g., Flesch–Kincaid) of the following text:\n\n"),
                Map.entry("detect_language", "Detect the language of the following text:\n\n"),
                Map.entry("keyword_density", "Calculate the keyword density for key terms in the following text:\n\n")
        );
        TEMPLATES = Collections.unmodifiableMap(m);
    }

    public Services(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }


    public String processContent(Request request) {
        // Build the prompt
        String prompt = buildPrompt(request);

        // Query the AI Model API
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );
        // Do request and get response
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse the response
        // Return response

        return extractTextFromResponse(response);
    }

    public String buildPrompt(Request request) {
        String operation = request.getOperation();
        String template = TEMPLATES.get(operation);
        if (template == null) {
            throw new IllegalArgumentException("Unknown Operation: " + operation);
        }
        // Append the template and the actual content in one go
        return template + request.getContent();
    }

    private String extractTextFromResponse(String response) {
        try {
            Response geminiResponse = objectMapper.readValue(response, Response.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                Response.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent() != null &&
                        firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().getFirst().getText();
                }
            }
            return "No content found in response";
        } catch (Exception e) {
            return "Error Parsing: " + e.getMessage();
        }
    }

}
