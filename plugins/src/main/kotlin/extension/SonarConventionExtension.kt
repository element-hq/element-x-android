/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.sonarqube.gradle.SonarExtension

fun Project.setupSonar() {
    plugins.apply("org.sonarqube")
    // To run a sonar analysis:
    // Run './gradlew sonar -Dsonar.login=<SONAR_LOGIN>'
    // The SONAR_LOGIN is stored in passbolt as Token Sonar Cloud Bma
    // Sonar result can be found here: https://sonarcloud.io/project/overview?id=element-x-android
    extensions.configure<SonarExtension> {
        properties {
            property("sonar.projectName", "element-x-android")
            property("sonar.projectKey", "element-x-android")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.projectVersion", "1.0") // TODO project(":app").android.defaultConfig.versionName)
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.links.homepage", "https://github.com/element-hq/element-x-android/")
            property("sonar.links.ci", "https://github.com/element-hq/element-x-android/actions")
            property("sonar.links.scm", "https://github.com/element-hq/element-x-android/")
            property("sonar.links.issue", "https://github.com/element-hq/element-x-android/issues")
            property("sonar.organization", "element-hq")
            property("sonar.login", if (project.hasProperty("SONAR_LOGIN")) project.property("SONAR_LOGIN")!! else "invalid")

            // exclude source code from analyses separated by a colon (:)
            // Exclude Java source and image resources
            property("sonar.exclusions", "**/BugReporterMultipartBody.java:**.png:**.webp")
            property("sonar.projectBaseDir", project.rootDir.absolutePath)
        }
    }
}
