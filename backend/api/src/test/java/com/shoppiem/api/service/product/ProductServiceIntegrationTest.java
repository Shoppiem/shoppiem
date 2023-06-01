package com.shoppiem.api.service.product;


import com.shoppiem.api.GenericResponse;
import com.shoppiem.api.ProductCreateResponse;
import com.shoppiem.api.ProductRequest;
import com.shoppiem.api.service.ServiceTestConfiguration;
import com.shoppiem.api.utils.migration.FlywayMigration;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Bizuwork Melesse
 * created on 5/26/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class ProductServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private FlywayMigration flywayMigration;

    @BeforeClass
    public void setup() {
        flywayMigration.migrate(true);
    }

    @AfterTest
    public void teardown() {
    }

    @SneakyThrows
    @BeforeMethod
    public void beforeEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has started");
    }

    @AfterMethod
    public void afterEachTest(Method method) {
        log.info("  Testcase: " + method.getName() + " has ended");
    }

    @Test
    public void createProductTest() {
      ProductRequest request = new ProductRequest()
          .productUrl("https://www.amazon.com/Belkin-Boost%E2%86%91ChargeTM-Wireless-Compatible-Kickstand/dp/B0BXRMCC31/?_encoding=UTF8&pd_rd_w=dyu4M&content-id=amzn1.sym.c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_p=c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_r=CVH3RSQQQDGMZPB008AQ&pd_rd_wg=B4Bru&pd_rd_r=08f1c561-28fd-45c0-8d24-ca21537303c7&ref_=pd_gw_gcx_gw_EGG-Graduation-23-1a&th=1");
      ProductCreateResponse response = productService.createProduct(request);
      assertNotNull(response);
      assertTrue(response.getInProgress());
    }

    @Test
    public void parseProductSKUTest() {
        String url = "https://www.amazon.com/Belkin-Boost%E2%86%91ChargeTM-Wireless-Compatible-Kickstand/dp/B0BXRMCC31/?_encoding=UTF8&pd_rd_w=dyu4M&content-id=amzn1.sym.c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_p=c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_r=CVH3RSQQQDGMZPB008AQ&pd_rd_wg=B4Bru&pd_rd_r=08f1c561-28fd-45c0-8d24-ca21537303c7&ref_=pd_gw_gcx_gw_EGG-Graduation-23-1a&th=1";
        String sku = productService.parseProductSku(url);
        assertNotNull(sku);
        assertEquals("B0BXRMCC31", sku);
    }

//    B07VWZHCNX

//    private Long createProject() {
//        ProjectEntity entity = new ProjectEntity();
//        entity.setProjectName("Demo TestNG project");
//        entity.setContentLink("https://www.instagram.com/p/CmGPqXuNvYG/?a=5");
//        entity.setContentType(KalicoContentType.FOOD_RECIPE.getValue());
//        entity.setParaphrase(true);
//        entity.setEmbedImages(false);
//        entity.setUserId(userId);
//        entity.setProcessed(true);
//        projectRepo.save(entity);
//
//        MediaContentEntity contentEntity = new MediaContentEntity();
//        contentEntity.setMediaId(ShoppiemUtils.generateUid());
//        contentEntity.setScrapedDescription(loadFromFile("text/description.txt"));
//        contentEntity.setScrapedTitle("Spicy egg fried rice");
//        contentEntity.setRawTranscript(loadFromFile("text/transcript.txt"));
//        contentEntity.setOnScreenText("");
//        contentEntity.setPermalink("https://www.instagram.com/reel/CmywGx6MYso/?igshid=YmMyMTA2M2Y=");
//        contentEntity.setProjectId(entity.getId());
//        mediaContentRepo.save(contentEntity);
//        return entity.getId();
//    }

//    @Test
//    public void chunkTextTest() {
//        String text = loadFromFile("text/transcript.txt");
//        List<String> response = languageService.chunkTranscript(text, 100);
//        assertNotNull(response);
//        assertEquals(5, response.size());
//    }
//
//    @SneakyThrows
//    private String loadFromFile(String path) {
//        InputStream resource = new ClassPathResource(path).getInputStream();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource)) ) {
//            return FileCopyUtils.copyToString(reader);
//        }
//    }
//    @Test(enabled = false)
//    public void onScreenTextCleanupTest() {
//      String onScreenText = "\n"
//          + "\n"
//          + " \n"
//          + "   \n"
//          + "\n"
//          + "WVIAMS OeInt Mnlinestel\n"
//          + "\n"
//          + "*\n"
//          + "\f\n"
//          + "0003.jpg\n"
//          + "----------\n"
//          + "Eien part thinly sliced\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "0005.jpg\n"
//          + "----------\n"
//          + "oS\n"
//          + "©\n"
//          + "&\n"
//          + "\n"
//          + "S\n"
//          + "q\n"
//          + "@\n"
//          + ">\n"
//          + "©\n"
//          + "©)\n"
//          + "Bo)\n"
//          + "TS\n"
//          + "©\n"
//          + "fo)\n"
//          + "Ke)\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "0006.jpg\n"
//          + "----------\n"
//          + "2 eggs and set aside\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "1-2 tosp red chilli flakes\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "0009.jpg\n"
//          + "----------\n"
//          + "Stir fry on high flame\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "0010.jpg\n"
//          + "----------\n"
//          + "a)\n"
//          + "@\n"
//          + "&\n"
//          + "©\n"
//          + "®\n"
//          + "—\n"
//          + "iS\n"
//          + "i)\n"
//          + "=\n"
//          + "=\n"
//          + "Ne\n"
//          + "S\n"
//          + "o)\n"
//          + "eS\n"
//          + "RS\n"
//          + "(ep)\n"
//          + ")\n"
//          + "©\n"
//          + "=y\n"
//          + "oy\n"
//          + "co\n"
//          + "ie\n"
//          + "ie\n"
//          + "<¢\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "0011.jpg\n"
//          + "----------\n"
//          + "Add sauces (check written recipe)\n"
//          + "\n"
//          + " \n"
//          + "\f\n"
//          + "0014.jpg\n"
//          + "----------\n"
//          + "Switch off the flame and mix\n"
//          + "\n"
//          + " \n"
//          + "\f\n";
//      String cleaned = languageService.cleanup(onScreenText);
//      assertThat(cleaned, is(notNullValue()));
//    }
//
//    @Test(enabled = false)
//    public void descriptionCleanupTest() {
//        String description = "Spicy egg fried rice \uD83D\uDD25 Recipe \uD83D\uDC47<br><br>INGREDIENTS:<br>5-6 green onions<br>6 cloves of garlic<br>1-2 tbsp crushed chilli flakes (add based on spice tolerance)<br>2 servings cold rice<br>2 eggs<br>Cooking oil as needed<br>—Sauce—<br>1 tbsp light/regular soy sauce<br>1 tbsp dark soy sauce<br>1 tbsp water<br>1 tsp sugar<br>Note: Add salt if needed<br><br>PROCESS:<br>1. Clean and trim 5-6 green onions. Mince the white parts and thinly slice the green parts for garnish<br>2. Mince 6 cloves of garlic. Add more or less depending on size of the cloves or taste<br>3. Take 2 servings of cold, day old rice in a bowl and separate the clumps. This helps in even stir frying. Cold, slightly dried out rice is essential for the perfect fried rice texture<br>4. Mix the sauce ingredients in a bowl and set aside<br>5. To a heated wok/pan add some oil and scramble 2 eggs with a pinch of salt. Set aside<br>6. Set flame to medium and add 2 tbsp neutral cooking oil. When the oil is hot, sauté the garlic and green onion for 30 seconds<br>7. Reduce flame to low and sauté 1-2 tbsp crushed red chilli flakes for 30 seconds or until fragrant. Chilli flakes will burn if the flame is too high<br>8. Increase flame to medium high, add rice and stir fry until mixed well with the aromatics<br>9. Add the prepared sauce and stir fry for 2-3 minutes. The rice needs to fry well so keep tossing and turning<br>10. When the rice is fried well, add the previously scrambled eggs. Mix everything well and adjust salt if needed<br>11. Add the green part of green onions and switch off the flame. You don’t want to overcook them<br>12. Serve the spicy egg fried rice with a crispy fried egg, a side of your choice or on its own<br><br>#easyrecipes #asmrcooking #recipes #friedrice";
//    }
//
//    @Test(enabled = false)
//    public void transcriptCleanupTest() {
//        String transcript = "";
//    }
}
