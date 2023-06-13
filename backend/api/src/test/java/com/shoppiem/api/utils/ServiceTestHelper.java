package com.shoppiem.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.UserProfile;
import com.shoppiem.api.data.postgres.entity.EmbeddingEntity;
import com.shoppiem.api.data.postgres.entity.ProductEntity;
import com.shoppiem.api.data.postgres.repo.EmbeddingRepo;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;

/**
 * @author Bizuwork Melesse
 * created on 2/17/22
 */
@Service
@RequiredArgsConstructor
public class ServiceTestHelper {

    public void prepareSecurity(String userId) {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(new UserProfile()
            .name("TestNG User")
            .email("testuser@exampe.com")
            .uid(userId == null ? UUID.randomUUID().toString() : userId)
        );

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    public static String loadFile(String fileName) throws IOException {
        File file = ResourceUtils.getFile("classpath:testData/" + fileName);
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static String loadFromFile(String path) {
        InputStream resource = null;
        try {
            resource = new ClassPathResource(path).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource)) ) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ProductEntity> loadProducts(boolean saveToDb, ObjectMapper objectMapper,
        ProductRepo productRepo) {
        TypeReference<List<ProductEntity>> typeRef = new TypeReference<>() {};
        try {
            List<ProductEntity> products = objectMapper.readValue(
                ServiceTestHelper.loadFromFile("scraper/tables/product.json"), typeRef);
            if (saveToDb) {
                productRepo.saveAll(products);
            }
            return products;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadEmbeddings(ObjectMapper objectMapper, EmbeddingRepo embeddingRepo) {
        TypeReference<List<EmbeddingEntity>> typeRef = new TypeReference<>() {};
        try {
            List<EmbeddingEntity> embeddingEntities = objectMapper.readValue(
                ServiceTestHelper.loadFromFile("scraper/tables/embedding.json"), typeRef);
            embeddingRepo.saveAll(embeddingEntities);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
