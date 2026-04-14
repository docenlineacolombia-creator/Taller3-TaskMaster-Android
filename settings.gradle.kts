/*
 * settings.gradle.kts
 *
 * Define:
 * 1. Los repositorios donde Gradle busca plugins (pluginManagement)
 * 2. Los repositorios donde Gradle busca dependencias (dependencyResolutionManagement)
 * 3. El nombre del proyecto y los submódulos incluidos
 *
 * SIN este archivo, Gradle no puede resolver com.android.application ni
 * org.jetbrains.kotlin.android, lo que causa el error:
 *   "Plugin was not found in any of the following sources"
 */
pluginManagement {
    repositories {
        // Repositorio oficial de plugins de Android / Google
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Repositorio central de Maven (Kotlin, Navigation SafeArgs, etc.)
        mavenCentral()
        // Repositorio de plugins de Gradle
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS evita que los módulos declaren sus propios repositorios
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Nombre del proyecto raíz (aparece en Android Studio)
rootProject.name = "Taller3-TaskMaster-Android"

// Módulos del proyecto
include(":app")
