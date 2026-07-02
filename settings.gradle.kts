pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Naver Map SDK
        maven("https://repository.map.naver.com/archive/maven")
        // Kakao SDK
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "TODAIT_AOS"
include(":app")
