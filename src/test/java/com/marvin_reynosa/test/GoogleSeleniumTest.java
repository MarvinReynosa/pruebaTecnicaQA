package com.marvin_reynosa.test;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.monte.media.Format;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.awt.*;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class GoogleSeleniumTest {
    private static final Logger LOGGER = Logger.getLogger(GoogleSeleniumTest.class.getName());

    private WebDriver driver;
    private WebDriverWait wait;
    private ScreenRecorder screenRecorder;
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String BASE_URL = "https://www.google.com";
    private static final String SEARCH_TERM = "Documentacion de selenium";

    @BeforeClass
    public void setup() {
        try {
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            File movieDir = new File("videos");

            if (!movieDir.exists()) {
                if (!movieDir.mkdirs()) {
                    System.err.println("No se pudo crear el directorio de videos");
                }
            }
            Format fileFormat = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);
            Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 24,
                    FrameRateKey, new Rational(15, 1),
                    QualityKey, 1.0f,
                    KeyFrameIntervalKey, 15 * 60);
            Format mouseFormat = new Format(MediaTypeKey, MediaType.VIDEO,
                    EncodingKey, "black",
                    FrameRateKey, new Rational(30, 1));
            Format audioFormat = null;

            screenRecorder = new MyScreenRecorder(gc,
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()),
                    fileFormat, screenFormat, mouseFormat, audioFormat, movieDir);
            screenRecorder.start();
            System.out.println("=== INICIANDO GRABACION DE VIDEO ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-web-security");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-features=VizDisplayCompositor");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-infobars");

        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);

        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});"
        );

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        createScreenshotDirectory();

        System.out.println("=== INICIANDO SESIÓN DE SELENIUM ===");
        System.out.println("Navegador: Chrome");
        System.out.println("URL Base: " + BASE_URL);
    }

    @Test
    public void testGoogleSearch() {
        System.out.println("\n--- Paso 1: Navegar a Google ---");
        driver.get(BASE_URL);
        waitForPageLoad();
        takeScreenshot("01_google_home");

        System.out.println("--- Paso 2: Buscar '" + SEARCH_TERM + "' ---");
        WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("q")));
        searchBox.sendKeys(SEARCH_TERM + Keys.ENTER);
        waitForPageLoad();
        takeScreenshot("02_google_results");

        checkForCaptcha();

        System.out.println("--- Paso 3: Hacer clic en enlace de documentación ---");
        clickSeleniumDocumentationLink();

        waitForPageLoad();
        takeScreenshot("03_selenium_documentation");
    }

    @Test(dependsOnMethods = "testGoogleSearch")
    public void testNavigateSideMenu() {
        System.out.println("\n--- Paso 4: Navegar por los items del menu lateral ---");

        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.contains("selenium.dev/documentation")) {
            System.out.println("No estamos en la pagina de documentacion. Navegando directamente...");
            driver.get("https://www.selenium.dev/documentation/");
            waitForPageLoad();
        }

        takeScreenshot("04_documentation_start");

        String[] menuItems = {
                "Overview",
                "WebDriver",
                "Selenium Manager",
                "Grid",
                "IE Driver Server",
                "IDE",
                "Test Practices",
                "Legacy"
        };

        for (String item : menuItems) {
            try {
                System.out.println("Navegando a: " + item);

                WebElement menuItem = findMenuItem(item);

                if (menuItem != null && menuItem.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                            menuItem
                    );

                    try {
                        menuItem.click();
                    } catch (ElementClickInterceptedException e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuItem);
                    }

                    waitForPageLoad();
                    takeScreenshot("success_" + item.replace(" ", "_"));
                    System.out.println("Navegación exitosa a: " + item);
                } else {
                    System.out.println("No se encontro el elemento del menu: " + item);
                    takeScreenshot("error_" + item.replace(" ", "_"));
                }
            } catch (Exception e) {
                System.err.println("Error al navegar a: " + item + " - " + e.getMessage());
                takeScreenshot("error_" + item.replace(" ", "_"));
            }
        }

        takeScreenshot("05_navigation_complete");
        System.out.println("=== Navegacion por el menu completada ===");
    }

    private void clickSeleniumDocumentationLink() {
        try {
            List<WebElement> links = driver.findElements(By.xpath("//a/h3|//a//h3"));
            boolean clicked = false;

            for (WebElement link : links) {
                String linkText = link.getText().toLowerCase();
                if (linkText.contains("selenium") &&
                        (linkText.contains("documentation") ||
                                linkText.contains("documentacion") ||
                                linkText.contains("docs"))) {
                    link.click();
                    clicked = true;
                    System.out.println("Enlace encontrado por texto: " + link.getText());
                    break;
                }
            }

            if (!clicked) {
                WebElement seleniumLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(@href, 'selenium.dev')]")));
                seleniumLink.click();
                System.out.println("Enlace encontrado por URL");
            }
        } catch (Exception e) {
            System.err.println("Error al hacer clic en el enlace: " + e.getMessage());
            takeScreenshot("error_clicking_link");
        }
    }

    private WebElement findMenuItem(String itemName) {
        String[] selectors = {
                "//a[contains(text(), '" + itemName + "')]",
                "//span[contains(text(), '" + itemName + "')]/..",
                "//div[contains(text(), '" + itemName + "')]/..",
                "//*[contains(@class, 'menu')]//*[contains(text(), '" + itemName + "')]",
                "//nav//*[contains(text(), '" + itemName + "')]",
                "//aside//*[contains(text(), '" + itemName + "')]",
                "//a[@class='td-sidebar-link td-sidebar-link__page ' and contains(text(), '" + itemName + "')]"
        };

        for (String selector : selectors) {
            try {
                WebElement element = driver.findElement(By.xpath(selector));
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    private void takeScreenshot(String filename) {
        if (driver != null) {
            try {
                File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.createDirectories(Paths.get(SCREENSHOT_DIR));

                Path destination = Paths.get(SCREENSHOT_DIR, filename + ".png");
                int counter = 1;
                while (Files.exists(destination)) {
                    destination = Paths.get(SCREENSHOT_DIR, filename + "_" + counter + ".png");
                    counter++;
                }

                Files.copy(srcFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Captura guardada: " + destination.getFileName());
            } catch (WebDriverException | IOException e) {
                System.err.println("No se pudo tomar la captura: " + filename);
            }
        }
    }

    private void waitForPageLoad() {
        try {
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
        } catch (TimeoutException e) {
            System.err.println("La pagina tardo demasiado en cargar.");
        }
    }

    private void checkForCaptcha() {
        String pageSource = driver.getPageSource().toLowerCase();
        if (pageSource.contains("captcha") ||
                pageSource.contains("robot") ||
                pageSource.contains("automated")) {
            System.out.println("ATENCION! Posible captcha detectado");
            takeScreenshot("posible_captcha");

            System.out.println("Esperando 5 segundos para resolucion manual...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void createScreenshotDirectory() {
        File directory = new File(SCREENSHOT_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.err.println("No se pudo crear el directorio: " + SCREENSHOT_DIR);
            } else {
                System.out.println("Directorio creado: " + directory.getAbsolutePath());
            }
        }
    }

    @AfterClass
    public void tearDown() {
        System.out.println("\n=== FINALIZANDO SESIÓN DE SELENIUM ===");

        if (driver != null) {
            driver.quit();
        }

        try {
            if (screenRecorder != null) {
                screenRecorder.stop();  // Detener la grabación
                System.out.println("=== GRABACIÓN DE VIDEO FINALIZADA ===");
                System.out.println("Videos guardados en: videos/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Screenshots guardados en: " + new File(SCREENSHOT_DIR).getAbsolutePath());
    }
}