plugins {
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "org.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        configurations.all {
            exclude(group = "com.zaxxer", module = "HikariCP")
            exclude(group = "ch.qos.logback", module = "logback-classic")
            exclude(group = "ch.qos.logback", module = "logback-core")
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
            exclude(group = "org.slf4j", module = "slf4j-jdk14")
            exclude(group = "org.slf4j", module = "slf4j-simple")
            exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
        }
    }
}
