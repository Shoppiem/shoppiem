package com.shoppiem.api.service.chat;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppiem.api.data.postgres.repo.EmbeddingRepo;
import com.shoppiem.api.data.postgres.repo.ProductAnswerRepo;
import com.shoppiem.api.data.postgres.repo.ProductQuestionRepo;
import com.shoppiem.api.data.postgres.repo.ProductRepo;
import com.shoppiem.api.data.postgres.repo.ReviewRepo;
import com.shoppiem.api.service.ServiceTestConfiguration;
import com.shoppiem.api.utils.ServiceTestHelper;
import com.shoppiem.api.utils.migration.FlywayMigration;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Bizuwork Melesse
 * created on 6/12/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class ChatServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private EmbeddingRepo embeddingRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductQuestionRepo questionRepo;

    @Autowired
    private ProductAnswerRepo answerRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private FlywayMigration flywayMigration;


    @BeforeClass
    public void setup() {
        flywayMigration.migrate(true);
        ServiceTestHelper.loadProducts(true, objectMapper, productRepo);
        ServiceTestHelper.loadReviews(objectMapper, reviewRepo);
        ServiceTestHelper.loadQuestions(objectMapper, questionRepo);
        ServiceTestHelper.loadAnswers(objectMapper, answerRepo);
        ServiceTestHelper.loadEmbeddings(objectMapper, embeddingRepo);
    }

    @AfterTest
    public void teardown() {
    }


    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void callGptTest() {
        String query = "What is the average battery life?";
        query = "what is the screen resolution for gaming?";
        String sku = "B0BW8K69VP";
        chatService.callGpt(query, sku);
    }

//    @Test(enabled = false)
//    public void createProductTest() throws InterruptedException {
//      ProductRequest request = new ProductRequest()
//          .productUrl("https://www.amazon.com/Sceptre-E248W-19203R-Monitor-Speakers-Metallic/dp/B0773ZY26F/ref=cm_cr_arp_d_product_top?ie=UTF8");
//      request.setProductUrl("https://www.amazon.com/dp/B0BW8K69VP/ref=sspa_dk_detail_2?psc=1&pd_rd_i=B0BW8K69VP&pd_rd_w=OJIWn&content-id=amzn1.sym.08ba9b95-1385-44b0-b652-c46acdff309c&pf_rd_p=08ba9b95-1385-44b0-b652-c46acdff309c&pf_rd_r=QFGGBS8QK2MSSHKTVH3X&pd_rd_wg=yjmoJ&pd_rd_r=5f62e5bf-5f96-4101-b5b4-4be74ed30baf&s=pc&sp_csd=d2lkZ2V0TmFtZT1zcF9kZXRhaWxfdGhlbWF0aWM&spLa=ZW5jcnlwdGVkUXVhbGlmaWVyPUEyUVhJMkRCMFE3Wlk0JmVuY3J5cHRlZElkPUEwNDU0NjUxMkg3WjNVTDNERjNJRiZlbmNyeXB0ZWRBZElkPUEwODUzNzg5M1ZSM0RaQ1lLNjNFUiZ3aWRnZXROYW1lPXNwX2RldGFpbF90aGVtYXRpYyZhY3Rpb249Y2xpY2tSZWRpcmVjdCZkb05vdExvZ0NsaWNrPXRydWU=");
//      ProductCreateResponse response = productService.createProduct(request);
//      assertNotNull(response);
//      assertTrue(response.getInProgress());
//      Thread.sleep(3600000);
//    }
//
//    @Test(enabled = false)
//    public void createProductFromDataTest() {
//        ProductEntity productEntity = productEntities
//            .stream()
//            .filter(it -> it.getProductSku().equals("B0BW8K69VP"))
//            .findFirst()
//            .get();
//        ProductFromDataRequest request = new ProductFromDataRequest()
//            .productUrl(productEntity.getProductUrl())
//            .currency(productEntity.getCurrency())
//            .description(productEntity.getDescription())
//            .price(productEntity.getPrice())
//            .numReviews(productEntity.getNumReviews())
//            .imageUrl(productEntity.getImageUrl())
//            .numQuestionsAnswered(productEntity.getNumQuestionsAnswered())
//            .seller(productEntity.getSeller())
//            .starRating(productEntity.getStarRating())
//            .title(productEntity.getTitle());
//        ProductCreateResponse response = productService.createProductFromData(request, false);
//        assertNotNull(response);
//        assertTrue(response.getInProgress());
//    }
//
//    @Test
//    public void scheduleJobsTest() throws InterruptedException {
//        String sku = "B0BW8K69VP";
//        productRepo.saveAll(productEntities);
//        ProductEntity productEntity = productRepo.findByProductSku(sku);
//        productService.scheduleJobs(productEntity);
//        Thread.sleep(3600000);
//    }
//
//    @Test(enabled = false)
//    public void parseProductSKUTest() {
//        String url = "https://www.amazon.com/Belkin-Boost%E2%86%91ChargeTM-Wireless-Compatible-Kickstand/dp/B0BXRMCC31/?_encoding=UTF8&pd_rd_w=dyu4M&content-id=amzn1.sym.c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_p=c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_r=CVH3RSQQQDGMZPB008AQ&pd_rd_wg=B4Bru&pd_rd_r=08f1c561-28fd-45c0-8d24-ca21537303c7&ref_=pd_gw_gcx_gw_EGG-Graduation-23-1a&th=1";
//        String sku = productService.parseProductSku(url);
//        assertNotNull(sku);
//        assertEquals("B0BXRMCC31", sku);
//    }
}
