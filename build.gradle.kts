plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"

    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "brightspark"
version = "1.1.0"

repositories {
    mavenCentral()
    maven {
        name = "Kord Extensions Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.7.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}
