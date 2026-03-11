plugins {
    id("java-library")
    id("maven-publish")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "io.nexstudios.menuservice"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))

    paperweight.paperDevBundle(providers.gradleProperty("paperVersion").get())

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}