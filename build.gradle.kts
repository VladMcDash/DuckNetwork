plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.postgresql:postgresql:42.5.4")
    implementation ("com.zaxxer:HikariCP:5.0.1")
    implementation ("org.postgresql:postgresql:42.7.0")

}

tasks.test {
    useJUnitPlatform()
}