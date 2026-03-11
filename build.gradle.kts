plugins {
    id("maven-publish")
    id("java")
    id("io.freefair.lombok") version "8.13.1" apply false

}

group = "io.nexstudios.menuservice"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        // PaperMC (UserDev / bundles)
        maven("https://repo.papermc.io/repository/maven-public/")

        // NexServiceRegistry (JitPack)
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")


    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}