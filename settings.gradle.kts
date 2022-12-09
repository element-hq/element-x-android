pluginManagement {
    repositories {
        includeBuild("plugins")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libraries/matrix/libs")
        }
    }
}
rootProject.name = "ElementX"
include(":app")
include(":libraries:core")
include(":libraries:rustsdk")
include(":libraries:matrix")
include(":libraries:textcomposer")
include(":libraries:elementresources")
include(":features:onboarding")
include(":features:login")
include(":features:roomlist")
include(":features:messages")
include(":libraries:designsystem")
