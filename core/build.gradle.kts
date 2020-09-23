plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.1")
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.3")
    implementation("net.jodah:expiringmap:0.5.9")
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set("FakeBlock-core-" + project.version + ".jar")
        relocate("co.aikar.taskchain", "pro.husk.fakeblock.taskchain")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
}