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
import org.jetbrains.kotlin.cli.common.toBooleanLenient

plugins {
    id("io.element.android-compose-application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    alias(libs.plugins.firebaseAppDistribution)
    alias(libs.plugins.knit)
    id("kotlin-parcelize")
    // To be able to update the firebase.xml files, uncomment and build the project
    // id("com.google.gms.google-services")
}

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
        named("debug") {
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
        named("debug") {
            resValue("string", "app_name", "Element X dbg")
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        named("release") {
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
                artifactPath = "$rootDir/app/build/outputs/apk/nightly/app-universal-nightly.apk"
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
            output.versionCode.set((output.versionCode.get() ?: 0) * 10 + abiCode)
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

/**
 * Kover configuration
 */

dependencies {
    // Add all sub projects to kover except some of them
    project.rootProject.subprojects
        .filter {
            it.project.projectDir.resolve("build.gradle.kts").exists()
        }
        .map { it.path }
        .sorted()
        .filter {
            it !in listOf(
                ":app",
                ":samples",
                ":anvilannotations",
                ":anvilcodegen",
                ":samples:minimal",
                ":tests:testutils",
                // Exclude `:libraries:matrix:impl` module, it contains only wrappers to access the Rust Matrix
                // SDK api, so it is not really relevant to unit test it: there is no logic to test.
                ":libraries:matrix:impl",
                // Exclude modules which are not Android libraries
                // See https://github.com/Kotlin/kotlinx-kover/issues/312
                ":appconfig",
                ":libraries:core",
                ":libraries:coroutines",
                ":libraries:di",
                ":libraries:rustsdk",
                ":libraries:textcomposer:lib",
            )
        }
        .forEach {
            // println("Add $it to kover")
            kover(project(it))
        }
}

val ciBuildProperty = "ci-build"
val isCiBuild = if (project.hasProperty(ciBuildProperty)) {
    val raw = project.property(ciBuildProperty) as? String
    raw?.toBooleanLenient() == true || raw?.toIntOrNull() == 1
} else {
    false
}

kover {
    // When running on the CI, run only debug test variants
    if (isCiBuild) {
        excludeTests {
            // Disable instrumentation for debug test tasks
            tasks(
                "testDebugUnitTest",
            )
        }
    }
}

// https://kotlin.github.io/kotlinx-kover/
// Run `./gradlew :app:koverHtmlReport` to get report at ./app/build/reports/kover
// Run `./gradlew :app:koverXmlReport` to get XML report
koverReport {
    filters {
        excludes {
            classes(
                // Exclude generated classes.
                "*_ModuleKt",
                "anvil.hint.binding.io.element.*",
                "anvil.hint.merge.*",
                "anvil.hint.multibinding.io.element.*",
                "anvil.module.*",
                "com.airbnb.android.showkase*",
                "io.element.android.libraries.designsystem.showkase.*",
                "io.element.android.x.di.DaggerAppComponent*",
                "*_Factory",
                "*_Factory_Impl",
                "*_Factory$*",
                "*_Module",
                "*_Module$*",
                "*Module_Provides*",
                "Dagger*Component*",
                "*ComposableSingletons$*",
                "*_AssistedFactory_Impl*",
                "*BuildConfig",
                // Generated by Showkase
                "*Ioelementandroid*PreviewKt$*",
                "*Ioelementandroid*PreviewKt",
                // Other
                // We do not cover Nodes (normally covered by maestro, but code coverage is not computed with maestro)
                "*Node",
                "*Node$*",
                "*Presenter\$present\$*",
                // Forked from compose
                "io.element.android.libraries.designsystem.theme.components.bottomsheet.*",
            )
            annotatedBy(
                "io.element.android.libraries.designsystem.preview.PreviewsDayNight",
                "io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight",
            )
        }
    }

    defaults {
        // add reports of both 'debug' and 'release' Android build variants to default reports
        mergeWith("debug")
        mergeWith("release")

        verify {
            onCheck = true
            // General rule: minimum code coverage.
            rule("Global minimum code coverage.") {
                isEnabled = true
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION
                bound {
                    minValue = 65
                    // Setting a max value, so that if coverage is bigger, it means that we have to change minValue.
                    // For instance if we have minValue = 20 and maxValue = 30, and current code coverage is now 31.32%, update
                    // minValue to 25 and maxValue to 35.
                    maxValue = 75
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.INSTRUCTION
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
            // Rule to ensure that coverage of Presenters is sufficient.
            rule("Check code coverage of presenters") {
                isEnabled = true
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.CLASS
                filters {
                    includes {
                        classes(
                            "*Presenter",
                        )
                    }
                    excludes {
                        classes(
                            "*Fake*Presenter",
                            "io.element.android.appnav.loggedin.LoggedInPresenter$*",
                            // Some options can't be tested at the moment
                            "io.element.android.features.preferences.impl.developer.DeveloperSettingsPresenter$*",
                            "*Presenter\$present\$*",
                        )
                    }
                }
                bound {
                    minValue = 85
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.INSTRUCTION
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
            // Rule to ensure that coverage of States is sufficient.
            rule("Check code coverage of states") {
                isEnabled = true
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.CLASS
                filters {
                    includes {
                        classes(
                            "^*State$",
                        )
                    }
                    excludes {
                        classes(
                            "io.element.android.appnav.root.RootNavState*",
                            "io.element.android.libraries.matrix.api.timeline.item.event.OtherState$*",
                            "io.element.android.libraries.matrix.api.timeline.item.event.EventSendState$*",
                            "io.element.android.libraries.matrix.api.room.RoomMembershipState*",
                            "io.element.android.libraries.matrix.api.room.MatrixRoomMembersState*",
                            "io.element.android.libraries.push.impl.notifications.NotificationState*",
                            "io.element.android.features.messages.impl.media.local.pdf.PdfViewerState",
                            "io.element.android.features.messages.impl.media.local.LocalMediaViewState",
                            "io.element.android.features.location.impl.map.MapState*",
                            "io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState*",
                            "io.element.android.libraries.designsystem.swipe.SwipeableActionsState*",
                            "io.element.android.features.messages.impl.timeline.components.ExpandableState*",
                            "io.element.android.features.messages.impl.timeline.model.bubble.BubbleState*",
                            "io.element.android.libraries.maplibre.compose.CameraPositionState*",
                            "io.element.android.libraries.maplibre.compose.SaveableCameraPositionState",
                            "io.element.android.libraries.maplibre.compose.SymbolState*",
                            "io.element.android.features.ftue.api.state.*",
                            "io.element.android.features.ftue.impl.welcome.state.*",
                        )
                    }
                }
                bound {
                    minValue = 90
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.INSTRUCTION
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
            // Rule to ensure that coverage of Views is sufficient (deactivated for now).
            rule("Check code coverage of views") {
                isEnabled = true
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.CLASS
                filters {
                    includes {
                        classes(
                            "*ViewKt",
                        )
                    }
                }
                bound {
                    // TODO Update this value, for now there are too many missing tests.
                    minValue = 0
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.INSTRUCTION
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }

    androidReports("release") {
    }
}

dependencies {
    allLibrariesImpl()
    allServicesImpl()
    allFeaturesImpl(rootDir, logger)
    implementation(projects.features.call)
    implementation(projects.anvilannotations)
    implementation(projects.appnav)
    implementation(projects.appconfig)
    anvil(projects.anvilcodegen)

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
}
