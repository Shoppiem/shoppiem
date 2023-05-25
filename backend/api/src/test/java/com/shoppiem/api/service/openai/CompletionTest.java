package com.shoppiem.api.service.openai;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.service.ServiceTestConfiguration;
import com.shoppiem.api.service.openai.completion.CompletionChoice;
import com.shoppiem.api.service.openai.completion.CompletionRequest;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class CompletionTest extends AbstractTestNGSpringContextTests {
    @Autowired
    private OpenAiProps openAiProps;
    private OpenAiService service;

    @BeforeClass
    public void setup() {
        service = new OpenAiService(openAiProps.getApiKey());
    }

    @Test
    void createCompletionWithDavinci() {
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt("Improve the writing style in this text.\n"
                    + "This Korean braised tofu is one of my favorite tofu dishes because it's super easy to make, plus it's packed full of flavor. It's also very spicy. I love spice, but if you don't like it too spicy, you can also add less chili powder and it's great with rice for a really hearty and complete meal. I really love to prepare this in advance because I usually do meal prep a week in advance and I like to make this in a big batch and store it in the refrigerator in a container so I can have whatever I want to enjoy.")
                .echo(false)
                .temperature(0.75)
                .n(1)
                .maxTokens(500)
                .user("testing")
                .logitBias(new HashMap<>())
                .build();

        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        assertEquals(1, choices.size());
    }

    @Test
    void createCompletionWithAda() {
        CompletionRequest completionRequest = CompletionRequest.builder()
            .model("ada")
            .prompt("Somebody once told me the world is gonna roll me")
            .echo(true)
            .n(5)
            .maxTokens(50)
            .user("testing")
            .logitBias(new HashMap<>())
            .build();

        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        assertEquals(5, choices.size());
    }

    @Test
    void createCompletionDeprecated() {
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("Somebody once told me the world is gonna roll me")
                .echo(true)
                .user("testing")
                .build();

        List<CompletionChoice> choices = service.createCompletion("ada", completionRequest).getChoices();
        assertFalse(choices.isEmpty());
    }
}
