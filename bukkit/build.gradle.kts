plugins {
    id("java-library")
    id("maven-publish")
}

group = "io.nexstudios.menuservice"
version = providers.gradleProperty("serviceVersion").get()

dependencies {

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT") {
        exclude(group = "org.apache.commons", module = "commons-lang3")
        exclude(group = "org.codehaus.plexus", module = "plexus-utils")
    }
    api("com.github.lightplugins:NexServiceRegistry:${providers.gradleProperty("registryVersion").get()}")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}