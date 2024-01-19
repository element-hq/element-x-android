import extension.applyKoverPluginToAllSubProjects

plugins {
    id("org.jetbrains.kotlinx.kover") apply false
}

applyKoverPluginToAllSubProjects()
