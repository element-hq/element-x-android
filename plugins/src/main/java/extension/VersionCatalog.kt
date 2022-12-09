package extension

import org.gradle.api.artifacts.VersionCatalog

private fun VersionCatalog.getVersion(alias: String) = findVersion(alias).get()

private fun VersionCatalog.getLibrary(library: String) = findLibrary(library).get()

private fun VersionCatalog.getBundle(bundle: String) = findBundle(bundle).get()

private fun VersionCatalog.getPlugin(plugin: String) = findPlugin(plugin).get()