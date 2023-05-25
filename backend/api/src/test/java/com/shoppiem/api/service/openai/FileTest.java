package com.shoppiem.api.service.openai;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import com.shoppiem.api.props.OpenAiProps;
import com.shoppiem.api.service.ServiceTestConfiguration;
import com.shoppiem.api.service.openai.file.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
public class FileTest extends AbstractTestNGSpringContextTests {
    static String filePath = "src/test/resources/openai/fine-tuning-data.jsonl";

    @Autowired
    private OpenAiProps openAiProps;
    private OpenAiService service;

    @BeforeClass
    public void setup() {
        service = new OpenAiService(openAiProps.getApiKey());
    }

    static String fileId;

    @Test
    void uploadFile() throws Exception {
        File file = service.uploadFile("fine-tune", filePath);
        fileId = file.getId();

        assertEquals("fine-tune", file.getPurpose());
        assertEquals(filePath, file.getFilename());

        // wait for file to be processed
        TimeUnit.SECONDS.sleep(10);
    }

    @Test(dependsOnMethods = {"uploadFile"})
    void listFiles() {
        List<File> files = service.listFiles();

        assertTrue(files.stream().anyMatch(file -> file.getId().equals(fileId)));
    }

    @Test(dependsOnMethods = {"listFiles"})
    void retrieveFile() {
        File file = service.retrieveFile(fileId);

        assertEquals(filePath, file.getFilename());
    }

    @Test(dependsOnMethods = {"retrieveFile"})
    void deleteFile() {
        DeleteResult result = service.deleteFile(fileId);
        assertTrue(result.isDeleted());
    }
}
