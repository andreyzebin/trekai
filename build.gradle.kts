plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("java")
}

group = "info.jtrac"
version = "3.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Database (using H2 as a modern replacement for HSQLDB)
    runtimeOnly("com.h2database:h2")

    // Other legacy dependencies - modern replacements
    implementation("org.jfree:jfreechart:1.5.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.lucene:lucene-core:9.6.0")
    implementation("org.apache.lucene:lucene-queryparser:9.6.0")
    
    // Ehcache 3 for caching
    implementation("javax.cache:cache-api:1.1.1")
    implementation("org.ehcache:ehcache:3.10.8")

    // SVNKit (replacement for javasvn)
    implementation("org.tmatesoft.svnkit:svnkit:1.10.10")

    // XML parsing
    implementation("dom4j:dom4j:1.6.1")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")

    

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // JWT Support
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Swagger / OpenAPI 3
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


