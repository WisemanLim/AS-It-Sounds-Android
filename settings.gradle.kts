// as-it-sounds-android — android-compose (정적 템플릿 / static scaffold)
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}
rootProject.name = "as-it-sounds-android"
include(":app")
