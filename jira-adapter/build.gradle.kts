plugins {
    id("java")
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("jakarta.resource:jakarta.resource-api:2.1.0")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Zip>("rar") {
    dependsOn(tasks.jar)
    archiveBaseName.set("jira-adapter")
    archiveFileName.set("jira-adapter.rar")
    archiveExtension.set("rar")

    from(tasks.jar)
    from(configurations.runtimeClasspath) {
        include("jackson-annotations-*.jar")
        include("jackson-core-*.jar")
        include("jackson-databind-*.jar")
    }
    into("META-INF") {
        from("src/main/rar/META-INF")
    }
}
