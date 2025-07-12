import extension.applyKoverPluginToAllSubProjects

class RootProjectPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        with(target) {
            project.applyKoverPluginToAllSubProjects()
        }
    }
}
