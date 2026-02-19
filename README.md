# Prueba Técnica QA - Selenium + TestNG + Gradle

## Descripción
Proyecto automatizado en Java utilizando:
- Selenium WebDriver
- TestNG
- Gradle
- WebDriverManager (Bonigarcia)

El test realiza lo siguiente:
1. Navega a Google
2. Busca "Documentacion de selenium"
3. Toma captura de pantalla de los resultados
4. Ingresa a https://www.selenium.dev/documentation/
5. Navega por cada ítem del menú lateral:
   - Overview
   - WebDriver
   - Selenium Manager
   - Grid
   - IE Driver Server
   - IDE
   - Test Practices
   - Legacy
   - About
6. Toma capturas de cada seccion
7. Graba la sesión completa en video

## Estructura del Proyecto
- `src/` → Código fuente
- `screenshots/` → Capturas generadas
- `videos/` → Grabación MP4 de la sesión
- `build.gradle` → Configuración del proyecto

---

## Cómo ejecutar

### Requisitos
- Java 17+
- Docker Desktop (para grabación)
- Gradle

### Ejecutar desde consola:

```bash
./gradlew test
