import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.0"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
	kotlin("plugin.jpa") version "1.9.20"
}

group = "com.donotfinishlast"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.h2database:h2:2.2.224")
	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
	implementation("org.springframework.boot:spring-boot-starter-web")

// https://mvnrepository.com/artifact/org.springframework/spring-websocket
	implementation("org.springframework.boot:spring-boot-starter-websocket")


	// https://mvnrepository.com/artifact/com.h2database/h2
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// https://mvnrepository.com/artifact/org.springframework/spring-messaging
	implementation("org.springframework:spring-messaging:6.1.0")

	// https://mvnrepository.com/artifact/org.modelmapper/modelmapper
	implementation("org.modelmapper:modelmapper:3.2.0")
	implementation("com.google.code.gson:gson:2.8.9")
	implementation("com.vladmihalcea:hibernate-types-60:2.20.0")



}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

