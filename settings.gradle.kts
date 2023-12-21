pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://github.com/jitsi/jitsi-maven-repository/raw/master/releases")
        }
        maven {
            url = uri("https://maven.google.com")
        }

    }
}

rootProject.name = "old_man"
include(":app")
