package com.enjay.llmservice;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the removeThinkTags method in Controller class.
 */
class ThinkTagRemovalTest {

    @Test
    void testRemoveThinkTags() throws Exception {
        // Create an instance of Controller for testing
        Controller controller = new Controller(null, null, null, null);

        // Get the private method using reflection
        Method removeThinkTagsMethod = Controller.class.getDeclaredMethod("removeThinkTags", String.class);
        removeThinkTagsMethod.setAccessible(true);

        // Test cases
        String input1 = "This is a normal response without tags";
        String input2 = "This is a response with <think>some thinking</think> tags";
        String input3 = "This has <think>multiple</think> think <think>tags</think> in it";
        String input4 = "<think>This starts with a think tag</think> and continues";
        String input5 = "This ends with a <think>think tag</think>";
        String input6 = null;

        // Expected outputs
        String expected1 = "This is a normal response without tags";
        String expected2 = "This is a response with  tags";
        String expected3 = "This has  think  in it";
        String expected4 = " and continues";
        String expected5 = "This ends with a ";
        String expected6 = null;

        // Invoke the method and assert results
        assertEquals(expected1, removeThinkTagsMethod.invoke(controller, input1));
        assertEquals(expected2, removeThinkTagsMethod.invoke(controller, input2));
        assertEquals(expected3, removeThinkTagsMethod.invoke(controller, input3));
        assertEquals(expected4, removeThinkTagsMethod.invoke(controller, input4));
        assertEquals(expected5, removeThinkTagsMethod.invoke(controller, input5));
        assertEquals(expected6, removeThinkTagsMethod.invoke(controller, input6));
    }

    @Test
    void testRemoveThink() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String input = """
                <think>

                Okay, the user wants me to translate the message "스프링으로 개발된 LLM 번역 애플리케이션 입니다." into Korean, English, Japanese, and Chinese. Let me start by understanding the original Korean sentence.\s

                First, "스프링으로 개발된" translates to "developed with Spring" or "a Spring-based application." Then "LLM 번역 애플리케이션" is "LLM translation application." So the whole sentence is "It is an LLM translation application developed with Spring."

                For the English translation, I should make sure to use the correct terminology. "Spring" here refers to the Spring framework, so it's important to mention that. Maybe "This is an LLM translation application developed using the Spring framework."

                Next, the Korean translation is straightforward since the original is in Korean. But I need to check if there's any nuance I should capture. The original uses "입니다" which is a polite form, so the translation should maintain that formality. "스프링으로 개발된 LLM 번역 애플리케이션입니다." Adding the ending "입니다" to the end.

                For Japanese, "Spring" is often translated as "スプリング" and "LLM" would stay as is. The structure would be similar: "LLM翻訳アプリケーションで、スプリングで開発されています。" But I should check the verb placement. Maybe "これは、スプリングで開発されたLLM翻訳アプリケーションです。" That sounds more natural.

                In Chinese, "Spring" is "Spring框架" and "LLM" remains. The sentence structure would be "这是一个使用Spring框架开发的LLM翻译应用程序。" Making sure the particles are correct, like "的" for possession and "使用" for using.

                I need to ensure that each translation is accurate and maintains the original meaning. Also, check for any grammatical errors or awkward phrasing in each language. Let me review each translation again to confirm.
                </think>
                {
                    "en": "This is an LLM translation application developed using the Spring framework.",
                    "ko": "스프링으로 개발된 LLM 번역 애플리케이션입니다.",
                    "cn": "这是一个使用Spring框架开发的LLM翻译应用程序。",
                    "jp": "これは、スプリングで開発されたLLM翻訳アプリケーションです。"
                }
                """;

        String expected = """
                {
                    "en": "This is an LLM translation application developed using the Spring framework.",
                    "ko": "스프링으로 개발된 LLM 번역 애플리케이션입니다.",
                    "cn": "这是一个使用Spring框架开发的LLM翻译应用程序。",
                    "jp": "これは、スプリングで開発されたLLM翻訳アプリケーションです。"
                }
                """;

        // Create an instance of Controller for testing
        Controller controller = new Controller(null, null, null, null);

        // Get the private method using reflection
        Method removeThinkTagsMethod = Controller.class.getDeclaredMethod("removeThinkTags", String.class);
        removeThinkTagsMethod.setAccessible(true);

        String result = (String) removeThinkTagsMethod.invoke(controller, input);

        // Assert that the result is not null
        Assertions.assertThat(result).isNotNull();

        // Assert that the <think> tags and their contents are removed
        assertEquals(expected.trim(), result.trim());
    }
}
