plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.4")
    }
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.data:spring-data-jpa")
}
