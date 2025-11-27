import org.gradle.api.tasks.JavaExec

plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.14"
}

javafx {
    version = "17"
    modules = listOf("javafx.controls", "javafx.fxml")
}
repositories {
    mavenCentral()
    mavenLocal() // optional: useful if you've published artifacts locally
}
application {
    //mainModule.set("ducknetwork")
    mainClass.set("ducknetwork.gui.DuckApplication")
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics")
}
tasks.withType(JavaExec::class.java).configureEach {
    modularity.inferModulePath.set(false)
}
tasks.named("run", JavaExec::class.java).configure {
    modularity.inferModulePath.set(false)
    classpath = sourceSets["main"].runtimeClasspath
}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.openjfx:javafx-controls:17.0.2:win")
    implementation("org.openjfx:javafx-fxml:17.0.2:win")
    implementation("org.postgresql:postgresql:42.7.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("org.controlsfx:controlsfx:11.1.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.5.0") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
}

tasks.test {
    useJUnitPlatform()
}