/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.FilterConfiguration.FilterType.ABI
import extension.allFeaturesImpl
import extension.allLibrariesImpl
import extension.allServicesImpl
import extension.gitBranchName
import extension.gitRevision
import extension.koverDependencies
import extension.setupKover

plugins {
    id("io.element.android-compose-application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    // When using precompiled plugins, we need to apply the firebase plugin like this
    id(libs.plugins.firebaseAppDistribution.get().pluginId)
    alias(libs.plugins.knit)
    id("kotlin-parcelize")
    // To be able to update the firebase.xml files, uncomment and build the project
    // id("com.google.gms.google-services")
}

setupKover()

android {
    namespace = "io.element.android.x"

    defaultConfig {
        applicationId = "io.element.android.x"
        targetSdk = Versions.targetSdk
        versionCode = Versions.versionCode
        versionName = Versions.versionName

        // Keep abiFilter for the universalApk
        ndk {
            abiFilters += listOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
        }

        buildConfigField("String", "GIT_REVISION", "\"${gitRevision()}\"")
        buildConfigField("String", "GIT_BRANCH_NAME", "\"${gitBranchName()}\"")

        // Ref: https://developer.android.com/studio/build/configure-apk-splits.html#configure-abi-split
        splits {
            // Configures multiple APKs based on ABI.
            abi {
                // Enables building multiple APKs per ABI.
                isEnable = true
                // By default all ABIs are included, so use reset() and include to specify that we only
                // want APKs for armeabi-v7a, x86, arm64-v8a and x86_64.
                // Resets the list of ABIs that Gradle should create APKs for to none.
                reset()
                // Specifies a list of ABIs that Gradle should create APKs for.
                include("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
                // Generate a universal APK that includes all ABIs, so user who installs from CI tool can use this one by default.
                isUniversalApk = true
            }
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

    buildTypes {
        getByName("debug") {
            resValue("string", "app_name", "Element X dbg")
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            resValue("string", "app_name", "Element X")
            signingConfig = signingConfigs.getByName("debug")

            postprocessing {
                isRemoveUnusedCode = true
                isObfuscate = false
                isOptimizeCode = true
                isRemoveUnusedResources = true
                proguardFiles("proguard-rules.pro")
            }
        }

        register("nightly") {
            val release = getByName("release")
            initWith(release)
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
            resValue("string", "app_name", "Element X nightly")
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("nightly")

            postprocessing {
                initWith(release.postprocessing)
            }

            firebaseAppDistribution {
                artifactType = "APK"
                // We upload the universal APK to fix this error:
                // "App Distribution found more than 1 output file for this variant.
                // Please contact firebase-support@google.com for help using APK splits with App Distribution."
                artifactPath = "$rootDir/app/build/outputs/apk/gplay/nightly/app-gplay-universal-nightly.apk"
                // artifactType = "AAB"
                // artifactPath = "$rootDir/app/build/outputs/bundle/nightly/app-nightly.aab"
                // This file will be generated by the GitHub action
                releaseNotesFile = "CHANGES_NIGHTLY.md"
                groups = "external-testers"
                // This should not be required, but if I do not add the appId, I get this error:
                // "App Distribution halted because it had a problem uploading the APK: [404] Requested entity was not found."
                appId = "1:912726360885:android:e17435e0beb0303000427c"
            }
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
    flavorDimensions += "store"
    productFlavors {
        create("gplay") {
            dimension = "store"
            isDefault = true
            buildConfigField("String", "SHORT_FLAVOR_DESCRIPTION", "\"G\"")
            buildConfigField("String", "FLAVOR_DESCRIPTION", "\"GooglePlay\"")
        }
        create("fdroid") {
            dimension = "store"
            buildConfigField("String", "SHORT_FLAVOR_DESCRIPTION", "\"F\"")
            buildConfigField("String", "FLAVOR_DESCRIPTION", "\"FDroid\"")
        }
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
            "*/towncrier/template.md",
            "**/CHANGES.md",
        )
    }
}

dependencies {
    allLibrariesImpl()
    allServicesImpl()
    allFeaturesImpl(rootDir, logger)
    implementation(projects.features.call)
    implementation(projects.features.migration.api)
    implementation(projects.anvilannotations)
    implementation(projects.appnav)
    implementation(projects.appconfig)
    anvil(projects.anvilcodegen)

    // Comment to not include firebase in the project
    "gplayImplementation"(projects.libraries.pushproviders.firebase)
    // Comment to not include unified push in the project
    implementation(projects.libraries.pushproviders.unifiedpush)

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

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)

    ksp(libs.showkase.processor)
    koverDependencies()
}
