import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "io.element.android.libraries.push.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.network.retrofit.bom))
    implementation(libs.network.retrofit)
    implementation(libs.serialization.json)
    implementation(libs.coil)

    implementation(libs.sqldelight.driver.android)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite)
    implementation(libs.sqldelight.coroutines)
    implementation(projects.libraries.encryptedDb)

    implementation(projects.appconfig)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.di)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.network)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.matrixmedia.api)
    implementation(projects.features.networkmonitor.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.troubleshoot.api)
    implementation(projects.libraries.workmanager.api)
    implementation(projects.features.call.api)
    implementation(projects.features.enterprise.api)
    implementation(projects.features.lockscreen.api)
    implementation(projects.libraries.featureflag.api)
    api(projects.libraries.pushproviders.api)
    api(projects.libraries.pushstore.api)
    api(projects.libraries.push.api)

    implementation(projects.services.analytics.api)
    implementation(projects.services.appnavstate.api)
    implementation(projects.services.toolbox.api)

    testCommonDependencies(libs)
    testImplementation(libs.coil.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.matrixmedia.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.libraries.pushproviders.test)
    testImplementation(projects.libraries.pushstore.test)
    testImplementation(projects.libraries.troubleshoot.test)
    testImplementation(projects.libraries.workmanager.test)
    testImplementation(projects.features.call.test)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.features.lockscreen.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.services.appnavstate.impl)
    testImplementation(projects.services.appnavstate.test)
    testImplementation(projects.services.toolbox.impl)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(libs.kotlinx.collections.immutable)
}

sqldelight {
    databases {
        create("PushDatabase") {
            schemaOutputDirectory = File("src/main/sqldelight/databases")
        }
    }
}
