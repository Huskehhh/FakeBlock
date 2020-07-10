plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":core"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set("FakeBlock-legacy-" + project.version + ".jar")
        relocate("co.aikar.taskchain", "pro.husk.fakeblock.taskchain")
    }

    build {
        dependsOn(shadowJar)
    }
}