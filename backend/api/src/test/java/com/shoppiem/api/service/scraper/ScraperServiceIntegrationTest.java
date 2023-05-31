package com.shoppiem.api.service.scraper;

import com.shoppiem.api.service.ServiceTestConfiguration;
import com.shoppiem.api.service.parser.AmazonParser;
import com.shoppiem.api.utils.migration.FlywayMigration;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Bizuwork Melesse
 * created on 5/26/23
 */
@Slf4j
@SpringBootTest(classes = ServiceTestConfiguration.class)
public class ScraperServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private AmazonParser amazonParser;

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

    @Test(enabled = false)
    public void getContentTest() {
        String url = "https://www.amazon.com/Belkin-Boost%E2%86%91ChargeTM-Wireless-Compatible-Kickstand/dp/B0BXRMCC31/?_encoding=UTF8&pd_rd_w=dyu4M&content-id=amzn1.sym.c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_p=c1df8aef-5b8d-403a-bbaa-0d55ea81081f&pf_rd_r=CVH3RSQQQDGMZPB008AQ&pd_rd_wg=B4Bru&pd_rd_r=08f1c561-28fd-45c0-8d24-ca21537303c7&ref_=pd_gw_gcx_gw_EGG-Graduation-23-1a&th=1";
        String sku = "B0BXRMCC31";
        scraperService.getContent(sku, url);
    }

    @Test(enabled = false)
    public void amazonAccessoryProductPageParserTest() {
        String sku = "B0BXRMCC31";
        String soup = loadFromFile("scraper/amazonProductPage_Accessories.html");
        amazonParser.processSoup(sku, soup);
    }

    @Test(enabled = false)
    public void amazonClothingProductPageParserTest() {
        String sku = "B0BJDTKPY1";
        String soup = loadFromFile("scraper/amazonProductPage_Clothing.html");
        amazonParser.processSoup(sku, soup);
    }

    @Test
    public void amazonBookProductPageParserTest() {
        String sku = "0385347863";
        String soup = loadFromFile("scraper/amazonProductPage_Books.html");
        amazonParser.processSoup(sku, soup);
    }

    @SneakyThrows
    private String loadFromFile(String path) {
        InputStream resource = new ClassPathResource(path).getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource)) ) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
