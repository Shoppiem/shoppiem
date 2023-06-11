package com.shoppiem.api.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Bizuwork Melesse
 * created on 1/31/23
 */
@Primary
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiProps {
    private int numRetries = 5;
    private String apiKey;
    private String embeddingModel;
    private long requestTimeoutSeconds = 120;



    private double temp = 0.5;
    private int maxTokens = 2048;
    private int chunkSize = 1024; //words
    private String model = "text-davinci-003";
    private String promptCluster = "Break the following text into paragraphs with headings and fix the grammar:";
    private String promptParagraph = "Break the following text into paragraphs and improve the writing style:";
    private String promptTitle = "Generate a blog post title for the following text:";
    private String promptRecipe = "Generate  recipe steps and ingredient list with quantity form the following text:";
    private String user = "shoppiem";
    private int descriptionCharacterLimit = 256;

    private int paragraphThresholdCharCount = 1000;
}
