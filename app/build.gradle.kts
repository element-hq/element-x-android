/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.FilterConfiguration.FilterType.ABI
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.GenerateBuildConfig
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import config.BuildTimeConfig
import extension.AssetCopyTask
import extension.GitBranchNameValueSource
import extension.GitRevisionValueSource
import extension.allEnterpriseImpl
import extension.allFeaturesImpl
import extension.allLibrariesImpl
import extension.allServicesImpl
import extension.buildConfigFieldStr
import extension.koverDependencies
import extension.locales
import extension.setupDependencyInjection
import extension.setupKover
import extension.testCommonDependencies
import java.util.Locale

plugins {
    id("io.element.android-compose-application")
    alias(libs.plugins.kotlin.android)
    // When using precompiled plugins, we need to apply the firebase plugin like this
    id(libs.plugins.firebaseAppDistribution.get().pluginId)
    alias(libs.plugins.knit)
    id("kotlin-parcelize")
    alias(libs.plugins.licensee)
    alias(libs.plugins.kotlin.serialization)
    // To be able to update the firebase.xml files, uncomment and build the project
    // alias(libs.plugins.gms.google.services)
}

setupKover()

android {
    namespace = "io.element.android.x"

    defaultConfig {
        applicationId = BuildTimeConfig.APPLICATION_ID
        targetSdk = Versions.TARGET_SDK
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME

        // Keep abiFilter for the universalApk
        ndk {
            abiFilters += listOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
        }

        // Ref: https://developer.android.com/studio/build/configure-apk-splits.html#configure-abi-split
        splits {
            // Configures multiple APKs based on ABI.
            abi {
                val buildingAppBundle = gradle.startParameter.taskNames.any { it.contains("bundle") }

                // Enables building multiple APKs per ABI. This should be disabled when building an AAB.
                isEnable = !buildingAppBundle

                // By default all ABIs are included, so use reset() and include to specify that we only
                // want APKs for armeabi-v7a, x86, arm64-v8a and x86_64.
                // Resets the list of ABIs that Gradle should create APKs for to none.
                reset()

                if (!buildingAppBundle) {
                    // Specifies a list of ABIs that Gradle should create APKs for.
                    include("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
                    // Generate a universal APK that includes all ABIs, so user who installs from CI tool can use this one by default.
                    isUniversalApk = true
                }
            }
        }

        androidResources {
            localeFilters += locales
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("./signature/debug.keystore")
            storePassword = "android"
        }
        register("nightly") {
            keyAlias = System.getenv("ELEMENT_ANDROID_NIGHTLY_KEYID")
                ?: project.property("signing.element.nightly.keyId") as? String?
            keyPassword = System.getenv("ELEMENT_ANDROID_NIGHTLY_KEYPASSWORD")
                ?: project.property("signing.element.nightly.keyPassword") as? String?
            storeFile = file("./signature/nightly.keystore")
            storePassword = System.getenv("ELEMENT_ANDROID_NIGHTLY_STOREPASSWORD")
                ?: project.property("signing.element.nightly.storePassword") as? String?
        }
    }

    val baseAppName = BuildTimeConfig.APPLICATION_NAME
    val buildType = if (isEnterpriseBuild) "Enterprise" else "FOSS"
    logger.warnInBox("Building ${defaultConfig.applicationId} ($baseAppName) [$buildType]")

    buildTypes {
        val oidcRedirectSchemeBase = BuildTimeConfig.METADATA_HOST_REVERSED ?: "io.element.android"
        getByName("debug") {
            resValue("string", "app_name", "$baseAppName dbg")
            resValue(
                "string",
                "login_redirect_scheme",
                "$oidcRedirectSchemeBase.debug",
            )
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            resValue("string", "app_name", baseAppName)
            resValue(
                "string",
                "login_redirect_scheme",
                oidcRedirectSchemeBase,
            )
            signingConfig = signingConfigs.getByName("debug")

            optimization {
                enable = true
                keepRules {
                    files.add(File(projectDir, "proguard-rules.pro"))
                    files.add(getDefaultProguardFile("proguard-android-optimize.txt"))
                }
            }
        }

        register("nightly") {
            val release = getByName("release")
            initWith(release)
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
            resValue("string", "app_name", "$baseAppName nightly")
            resValue(
                "string",
                "login_redirect_scheme",
                "$oidcRedirectSchemeBase.nightly",
            )
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("nightly")

            firebaseAppDistribution {
                artifactType = "APK"
                // We upload the universal APK to fix this error:
                // "App Distribution found more than 1 output file for this variant.
                // Please contact firebase-support@google.com for help using APK splits with App Distribution."
                artifactPath = "$rootDir/app/build/outputs/apk/gplay/nightly/app-gplay-universal-nightly.apk"
                // artifactType = "AAB"
                // artifactPath = "$rootDir/app/build/outputs/bundle/nightly/app-nightly.aab"
                releaseNotesFile = "tools/release/ReleaseNotesNightly.md"
                groups = if (isEnterpriseBuild) {
                    "enterprise-testers"
                } else {
                    "external-testers"
                }
                // This should not be required, but if I do not add the appId, I get this error:
                // "App Distribution halted because it had a problem uploading the APK: [404] Requested entity was not found."
                appId = if (isEnterpriseBuild) {
                    "1:912726360885:android:3f7e1fe644d99d5a00427c"
                } else {
                    "1:912726360885:android:e17435e0beb0303000427c"
                }
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }
    flavorDimensions += "store"
    productFlavors {
        create("gplay") {
            dimension = "store"
            isDefault = true
            buildConfigFieldStr("SHORT_FLAVOR_DESCRIPTION", "G")
            buildConfigFieldStr("FLAVOR_DESCRIPTION", "GooglePlay")
        }
        create("fdroid") {
            dimension = "store"
            buildConfigFieldStr("SHORT_FLAVOR_DESCRIPTION", "F")
            buildConfigFieldStr("FLAVOR_DESCRIPTION", "FDroid")
        }
    }

    packaging {
        resources.pickFirsts += setOf(
            "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
        )
    }
}

androidComponents {
    // map for the version codes last digit
    // x86 must have greater values than arm
    // 64 bits have greater value than 32 bits
    val abiVersionCodes = mapOf(
        "armeabi-v7a" to 1,
        "arm64-v8a" to 2,
        "x86" to 3,
        "x86_64" to 4,
    )

    onVariants { variant ->
        // Assigns a different version code for each output APK
        // other than the universal APK.
        variant.outputs.forEach { output ->
            val name = output.filters.find { it.filterType == ABI }?.identifier

            // Stores the value of abiCodes that is associated with the ABI for this variant.
            val abiCode = abiVersionCodes[name] ?: 0
            // Assigns the new version code to output.versionCode, which changes the version code
            // for only the output APK, not for the variant itself.
            output.versionCode.set((output.versionCode.orNull ?: 0) * 10 + abiCode)
        }
    }

    val reportingExtension: ReportingExtension = project.extensions.getByType(ReportingExtension::class.java)
    configureLicensesTasks(reportingExtension)
}

// Knit
apply {
    plugin("kotlinx-knit")
}

knit {
    files = fileTree(project.rootDir) {
        include(
            "**/*.md",
            "**/*.kt",
            "*/*.kts",
        )
        exclude(
            "**/build/**",
            "*/.gradle/**",
            "**/CHANGES.md",
        )
    }
}

setupDependencyInjection()

dependencies {
    allLibrariesImpl()
    allServicesImpl()
    if (isEnterpriseBuild) {
        allEnterpriseImpl(project)
        implementation(projects.appicon.enterprise)
    } else {
        implementation(projects.features.enterprise.implFoss)
        implementation(projects.appicon.element)
    }
    allFeaturesImpl(project)
    implementation(projects.features.migration.api)
    implementation(projects.appnav)
    implementation(projects.appconfig)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.analytics.compose)

    if (ModulesConfig.pushProvidersConfig.includeFirebase) {
        "gplayImplementation"(projects.libraries.pushproviders.firebase)
    }
    if (ModulesConfig.pushProvidersConfig.includeUnifiedPush) {
        implementation(projects.libraries.pushproviders.unifiedpush)
    }

    implementation(libs.appyx.core)
    implementation(libs.androidx.splash)
    implementation(libs.androidx.core)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.preference)
    implementation(libs.coil)

    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.logging)
    implementation(libs.serialization.json)

    implementation(libs.matrix.emojibase.bindings)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.toolbox.test)

    koverDependencies()
}

tasks.withType<GenerateBuildConfig>().configureEach {
    outputs.upToDateWhen { false }
    val gitRevision = providers.of(GitRevisionValueSource::class.java) {}.get()
    val gitBranchName = providers.of(GitBranchNameValueSource::class.java) {}.get()
    android.defaultConfig.buildConfigFieldStr("GIT_REVISION", gitRevision)
    android.defaultConfig.buildConfigFieldStr("GIT_BRANCH_NAME", gitBranchName)
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    allow("EPL-1.0")
    allowUrl("https://opensource.org/licenses/MIT")
    allowUrl("https://developer.android.com/studio/terms.html")
    allowUrl("https://www.zetetic.net/sqlcipher/license/")
    allowUrl("https://jsoup.org/license")
    allowUrl("https://asm.ow2.io/license.html")
    allowUrl("https://www.gnu.org/licenses/agpl-3.0.txt")
    allowUrl("https://github.com/mhssn95/compose-color-picker/blob/main/LICENSE")
    ignoreDependencies("com.github.matrix-org", "matrix-analytics-events")
    // Ignore dependency that are not third-party licenses to us.
    ignoreDependencies(groupId = "io.element.android")
}

fun Project.configureLicensesTasks(reportingExtension: ReportingExtension) {
    androidComponents {
        onVariants { variant ->
            val capitalizedVariantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }
            val artifactsFile = reportingExtension.baseDirectory.file("licensee/android$capitalizedVariantName/artifacts.json")

            val copyArtifactsTask =
                project.tasks.register<AssetCopyTask>("copy${capitalizedVariantName}LicenseeReportToAssets") {
                    inputFile.set(artifactsFile)
                    targetFileName.set("licensee-artifacts.json")
                }
            variant.sources.assets?.addGeneratedSourceDirectory(
                copyArtifactsTask,
                AssetCopyTask::outputDirectory,
            )
            copyArtifactsTask.dependsOn("licenseeAndroid$capitalizedVariantName")
        }
    }
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            val tink = libs.google.tink.get()
            substitute(module("com.google.crypto.tink:tink")).using(module("${tink.group}:${tink.name}:${tink.version}"))
        }
    }
}
