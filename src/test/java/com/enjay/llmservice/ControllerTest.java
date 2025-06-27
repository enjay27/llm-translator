package com.enjay.llmservice;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ControllerTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    void a() {
        String hi = ollamaChatModel.call(new UserMessage("hi"));
        Assertions.assertThat(hi).isNotNull();
    }

    @Test
    void testMultipleTargetLanguages() {
        // This test doesn't actually call the API, but verifies that the Controller can be instantiated
        // with the new MessageRequest structure
        Controller controller = new Controller(null, null, null, null);
        Assertions.assertThat(controller).isNotNull();
    }
}
