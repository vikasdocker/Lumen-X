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
    }
}

rootProject.name = "LumenX"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:hardware")
include(":core:model")
include(":core:ui")
include(":feature:flashlight")
include(":feature:modes")
include(":feature:settings")
include(":feature:timer")
