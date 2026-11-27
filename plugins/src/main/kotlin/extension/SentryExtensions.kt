/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import io.sentry.android.gradle.extensions.SentryPluginExtension
import isEnterpriseBuild
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Setup the Sentry Gradle plugin to upload the R8 mapping file to Sentry, to be able to retrace the obfuscated
 * stack traces of the GPlay builds. Must be called from the application module.
 *
 * The plugin is only applied when an auth token is provided (release and nightly CI), so local and PR builds
 * are not impacted. The auth token is read from the environment variable `SENTRY_AUTH_TOKEN`,
 * or, for Element X only, from the Gradle property `services.analyticsproviders.sentry.authToken` (for instance set in
 * `~/.gradle/gradle.properties`). An empty value is ignored, which is the value of a missing secret on the CI.
 */
fun Project.setupSentryMappingUpload() {
    // The Gradle property is only used for Element X, other builds only get the token from the environment.
    val sentryAuthToken = if (isEnterpriseBuild) {
        providers.environmentVariable("SENTRY_AUTH_TOKEN").orNull
    } else {
        environmentOrGradleValue(
            environmentVariableName = "SENTRY_AUTH_TOKEN",
            gradlePropertyName = "services.analyticsproviders.sentry.authToken",
        )
    }
    if (sentryAuthToken.isNullOrEmpty()) return

    pluginManager.apply("io.sentry.android.gradle")
    extensions.configure<SentryPluginExtension> {
        authToken.set(sentryAuthToken)
        org.set("element")
        projectName.set("element-x-android")
        url.set("https://sentry.tools.element.io")
        includeProguardMapping.set(true)
        autoUploadProguardMapping.set(true)
        // F-Droid builds are not obfuscated, and debug builds are not minified.
        ignoredFlavors.set(setOf("fdroid"))
        ignoredBuildTypes.set(setOf("debug"))
        // Only use the plugin to upload the mapping file, disable everything else.
        // The Sentry dependency is added by the module services:analyticsproviders:sentry.
        autoInstallation { enabled.set(false) }
        // No bytecode rewriting.
        tracingInstrumentation { enabled.set(false) }
        runtimeOptimizations { enabled.set(false) }
        includeDependenciesReport.set(false)
        includeSourceContext.set(false)
        uploadNativeSymbols.set(false)
        // Do not send build telemetry to Sentry.
        telemetry.set(false)
    }
}
