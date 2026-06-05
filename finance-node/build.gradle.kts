plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("war")
}

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.apache.activemq:activemq-client-jakarta")
    runtimeOnly("org.postgresql:postgresql")

    implementation("org.springframework:spring-jms")
    runtimeOnly("org.jboss.slf4j:slf4j-jboss-logmanager:1.2.0.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2:2.2.224")
}

tasks.named<War>("war") {
    archiveFileName.set("finance-node.war")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    enabled = false
}
