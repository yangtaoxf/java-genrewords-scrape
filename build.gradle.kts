import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("application")
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

application {
    mainClassName = "com.github.ghostbear.javagenrewordscrape.Application"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile("org.jsoup:jsoup:1.12.1")
    compile("org.neo4j.driver:neo4j-java-driver:1.7.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to application.mainClassName))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
