plugins {
    id("java")
    id("application") // Necesar pentru aplicații JavaFX
    id("org.openjfx.javafxplugin") version "0.1.0" // Plugin-ul JavaFX
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configurare JavaFX (versiunile din exemplul tău)
javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}


dependencies {
    // Testare
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Bază de date (am păstrat versiunea mai nouă și HikariCP)
    implementation("org.postgresql:postgresql:42.7.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Biblioteci UI suplimentare (din exemplul lab6-gui pe care l-ai trimis)
    // Le poți șterge dacă nu le folosești, dar sunt utile pentru controale avansate
    implementation("org.controlsfx:controlsfx:11.1.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.5.0") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
}

tasks.test {
    useJUnitPlatform()
}