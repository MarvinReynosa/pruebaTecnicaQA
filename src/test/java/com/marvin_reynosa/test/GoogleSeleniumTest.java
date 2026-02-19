package com.marvin_reynosa.test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GoogleSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String BASE_URL = "https://www.google.com";
    //private static final String BASE_URL = "https://www.selenium.dev/documentation/";
    private static final String SEARCH_TERM = "Documentacion de selenium";
    //https://www.selenium.dev/documentation/
    private Random random = new Random();

    @BeforeClass
    public void setUp() {
        System.out.println("=== INICIANDO SESION DE SELENIUM ===");

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-web-security");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);

        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        createScreenshotDirectory();

        System.out.println("Navegador: Chrome (modo anti-deteccion)");
        System.out.println("URL Base: " + BASE_URL);
    }

    @Test(priority = 1)
    public void testGoogleSearch() throws InterruptedException, IOException {
        System.out.println("\n--- Paso 1: Navegar a Google ---");
        driver.get(BASE_URL);

        humanPause(2000, 500);

        takeScreenshot("00_pagina_principal.png");

        handleCookiesGoogle();

        System.out.println("--- Paso 2: Buscar '" + SEARCH_TERM + "' ---");

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        typeLikeHuman(searchBox, SEARCH_TERM);

        humanPause(1000, 200);

        searchBox.sendKeys(Keys.ENTER);

        humanPause(3000, 500);
        takeScreenshot("01_resultados_busqueda.png");

        checkForCaptcha();
    }

    @Test(priority = 2, dependsOnMethods = "testGoogleSearch")
    public void testClickDocumentationLink() throws InterruptedException, IOException {
        System.out.println("\n--- Paso 3: Buscar enlace de documentacion de Selenium ---");

        checkForCaptcha();

        humanPause(2000, 300);
        boolean found = findSeleniumDocumentationLink();

        if (!found) {
            System.out.println("No se encontro el enlace especifico, buscando alternativas...");

            found = findSeleniumLinkAlternative();
        }

        if (!found) {
            throw new RuntimeException("No se pudo encontrar el enlace de documentacion de Selenium");
        }

        humanPause(3000, 500);

        System.out.println("--- Paso 4: Capturar pantalla de documentacion ---");
        takeScreenshot("02_documentacion_selenium.png");
    }

   /* @Test(priority = 3, dependsOnMethods = "testClickDocumentationLink")
    public void testNavigateSideMenu() throws InterruptedException, IOException {
        System.out.println("\n--- Paso 5: Navegar por los items del menu lateral ---");

        humanPause(3000, 500);

        takeScreenshot("03_pagina_documentacion_inicio.png");

        String[] menuItems = {
                "Overview",
                "WebDriver",
                "Grid",
                "IE Driver Server",
                "Selenium IDE"
        };

        int itemCount = 1;
        for (String menuItem : menuItems) {
            try {
                System.out.println("Navegando a: " + menuItem);

                humanPause(1000, 200);

                // Intentar diferentes selectores para el menú
                WebElement menuElement = findMenuItem(menuItem);

                if (menuElement == null) {
                    System.out.println("No se encontro: " + menuItem + ", continuando con el siguiente...");
                    continue;
                }

                // Scroll suave hacia el elemento
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                        menuElement
                );

                humanPause(1000, 300);

                try {
                    menuElement.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuElement);
                }

                humanPause(2000, 500);

                takeScreenshot("04_menu_" + itemCount + "_" + menuItem.replace(" ", "_") + ".png");

                navigateBackToDocumentation();

                itemCount++;

            } catch (Exception e) {
                System.out.println("Error al navegar a: " + menuItem + " - " + e.getMessage());
                takeScreenshot("error_" + menuItem.replace(" ", "_") + ".png");
            }
        }

        takeScreenshot("05_navegacion_completada.png");
        System.out.println("=== Navegacion completada ===");
    }*/
   @Test(priority = 3, dependsOnMethods = "testClickDocumentationLink")
   public void testNavigateSideMenu() throws InterruptedException, IOException {
       System.out.println("\n--- Paso 5: Navegar por los items del menu lateral de la documentacion ---");

       // Verificar que estamos en la URL correcta de documentación
       String currentUrl = driver.getCurrentUrl();
       if (!currentUrl.contains("selenium.dev/documentation")) {
           System.out.println("No estamos en la página de documentación. Navegando directamente...");
           driver.get("https://www.selenium.dev/documentation/");
       }

       humanPause(3000, 500);
       takeScreenshot("03_pagina_documentacion_inicio.png");
       String[] menuItems = {
               "Overview",
               "WebDriver",
               "Grid",
               "IE Driver Server",
               "Selenium IDE"
       };

       int itemCount = 1;
       for (String menuItem : menuItems) {
           try {
               System.out.println("Navegando a: " + menuItem);

               humanPause(1000, 200);

               WebElement menuElement = findMenuItem(menuItem);

               if (menuElement == null) {
                   System.out.println("No se encontro: " + menuItem + ", continuando con el siguiente...");
                   continue;
               }
               ((JavascriptExecutor) driver).executeScript(
                       "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                       menuElement
               );

               humanPause(1000, 300);

               try {
                   menuElement.click();
               } catch (Exception e) {
                   ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuElement);
               }
               humanPause(3000, 500);

               takeScreenshot("04_menu_" + itemCount + "_" + menuItem.replace(" ", "_") + ".png");

               System.out.println("Navegacion a " + menuItem + " completada");
               itemCount++;

           } catch (Exception e) {
               System.out.println("Error al navegar a: " + menuItem + " - " + e.getMessage());
               takeScreenshot("error_" + menuItem.replace(" ", "_") + ".png");
           }
       }
       takeScreenshot("05_navegacion_completada.png");
       System.out.println("=== Navegacion por el menu completada ===");
   }

    @AfterClass
    public void tearDown() {
        System.out.println("\n=== FINALIZANDO SESION DE SELENIUM ===");
        System.out.println("Screenshots guardados en: " + new File(SCREENSHOT_DIR).getAbsolutePath());

        if (driver != null) {
            humanPause(2000, 0);
            driver.quit();
        }
    }

    private void humanPause(int base, int randomRange) {
        try {
            Thread.sleep(base + random.nextInt(randomRange));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void typeLikeHuman(WebElement element, String text) {
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            humanPause(50, 150);
        }
    }

    private void handleCookiesGoogle() {
        try {
            String[] cookieSelectors = {
                    "//button[contains(.,'Aceptar')]",
                    "//button[contains(.,'Accept')]",
                    "//button[contains(.,'Acepto')]",
                    "//button[@id='L2AGLb']",
                    "//div[contains(text(),'Aceptar')]",
                    "//form//button"
            };

            for (String selector : cookieSelectors) {
                try {
                    WebElement cookieButton = driver.findElement(By.xpath(selector));
                    if (cookieButton.isDisplayed()) {
                        cookieButton.click();
                        System.out.println("Cookies aceptadas con selector: " + selector);
                        humanPause(1000, 500);
                        break;
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.out.println("No se encontro dialogo de cookies o ya estaba aceptado");
        }
    }

    private void checkForCaptcha() throws IOException {
        String pageSource = driver.getPageSource().toLowerCase();
        if (pageSource.contains("captcha") ||
                pageSource.contains("robot") ||
                pageSource.contains("automated")) {
            System.out.println("ATENCION! Posible captcha detectado");
            takeScreenshot("posible_captcha.png");

            System.out.println("Esperando 5 segundos por si hay que resolver captcha manualmente...");
            humanPause(3000, 0);
        }
    }

    private boolean findSeleniumDocumentationLink() {
        try {
            List<WebElement> links = driver.findElements(By.xpath("//a/h3|//a//h3"));

            for (WebElement link : links) {
                String linkText = link.getText().toLowerCase();
                if (linkText.contains("selenium") &&
                        (linkText.contains("documentation") ||
                                linkText.contains("documentacion") ||
                                linkText.contains("docs"))) {
                    System.out.println("Encontrado (texto): " + link.getText());
                    link.click();
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            System.out.println("Error en busqueda primaria: " + e.getMessage());
            return false;
        }
    }

    private boolean findSeleniumLinkAlternative() {
        try {
            List<WebElement> links = driver.findElements(By.xpath("//a[contains(@href, 'selenium')]"));

            for (WebElement link : links) {
                String href = link.getAttribute("href");
                String text = link.getText().toLowerCase();

                if ((href != null && href.contains("selenium.dev")) ||
                        text.contains("documentation") ||
                        text.contains("docs")) {
                    System.out.println("Encontrado (URL): " + text);
                    link.click();
                    return true;
                }
            }

            WebElement organicResult = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@id='search']//a[contains(@href, 'selenium')]")
            ));

            if (organicResult != null) {
                System.out.println("Encontrado en resultados orgánicos");
                organicResult.click();
                return true;
            }

            return false;
        } catch (Exception e) {
            System.out.println("Error en busqueda alternativa: " + e.getMessage());
            return false;
        }
    }

    private WebElement findMenuItem(String itemName) {
        String[] selectors = {
                "//a[contains(text(), '" + itemName + "')]",
                "//span[contains(text(), '" + itemName + "')]/..",
                "//div[contains(text(), '" + itemName + "')]/..",
                "//*[contains(@class, 'menu')]//*[contains(text(), '" + itemName + "')]",
                "//nav//*[contains(text(), '" + itemName + "')]",
                "//aside//*[contains(text(), '" + itemName + "')]"
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

    private void navigateBackToDocumentation() {
        try {
            WebElement homeLink = driver.findElement(By.xpath(
                    "//a[contains(@href, 'selenium.dev')] | //a[contains(text(), 'Selenium')] | //a[contains(@class, 'logo')]"
            ));
            homeLink.click();
            humanPause(2000, 500);
        } catch (Exception e) {
            driver.navigate().back();
            humanPause(2000, 500);
        }
    }

    private void takeScreenshot(String filename) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Path destination = Paths.get(SCREENSHOT_DIR, filename);
        Files.copy(screenshot.toPath(), destination);
        System.out.println("Screenshot guardado: " + filename);
    }

    private void createScreenshotDirectory() {
        File directory = new File(SCREENSHOT_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Directorio creado: " + directory.getAbsolutePath());
        }
    }
}