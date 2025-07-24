plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("java")
    war
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
    implementation("org.springframework.security:spring-security-cas")
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

    // Provided for WAR deployment
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<War>("war") {
    archiveFileName.set("jtrac.war")
}
