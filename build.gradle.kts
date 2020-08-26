allprojects {
    group = "pro.husk.fakeblock"
    val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
    version = project.property("version").toString() + "-SNAPSHOT"

    if (buildNumber != null) {
        version = "$version-b$buildNumber"
    }
}

plugins {
    java
}

defaultTasks("shadow")

subprojects {

    apply(plugin = "java")

    repositories {
        mavenCentral()
        mavenLocal()

        maven {
            name = "Spigot"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }

        maven {
            name = "Bungeecord chat repo"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "ProtocolLib repo"
            url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
        }

        maven {
            name = "Aikar repo"
            url = uri("https://repo.aikar.co/content/groups/aikar/")
        }

        maven {
            name = "husK repo"
            url = uri("https://maven.husk.pro/repository/maven-public/")
        }
    }

    dependencies {
        implementation("co.aikar:taskchain-bukkit:3.7.2")
        compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
        compileOnly("org.projectlombok:lombok:1.18.12")
        annotationProcessor("org.projectlombok:lombok:1.18.12")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
