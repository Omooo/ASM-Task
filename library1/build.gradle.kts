plugins {
    kotlin("jvm") version "1.7.10"
}

sourceSets {
    main {
        java {
            srcDir("src/main")
        }
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}