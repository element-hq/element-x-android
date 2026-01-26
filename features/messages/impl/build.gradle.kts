import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.messages.impl"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    api(projects.features.messages.api)
    implementation(projects.appconfig)
    implementation(projects.features.call.api)
    implementation(projects.features.enterprise.api)
    implementation(projects.features.forward.api)
    implementation(projects.features.location.api)
    implementation(projects.features.poll.api)
    implementation(projects.features.roomcall.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.matrixmedia.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.textcomposer.impl)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.eventformatter.api)
    implementation(projects.libraries.mediapickers.api)
    implementation(projects.libraries.mediaviewer.api)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.mediaupload.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.recentemojis.api)
    implementation(projects.libraries.roomselect.api)
    implementation(projects.libraries.voiceplayer.api)
    implementation(projects.libraries.voicerecorder.api)
    implementation(projects.libraries.mediaplayer.api)
    implementation(projects.libraries.push.api)
    implementation(projects.libraries.uiUtils)
    implementation(projects.libraries.testtags)
    implementation(projects.features.networkmonitor.api)
    implementation(projects.services.analytics.compose)
    implementation(projects.services.appnavstate.api)
    implementation(projects.services.toolbox.api)
    implementation(libs.coil.compose)
    implementation(libs.datetime)
    implementation(libs.jsoup)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.sigpwned.emoji4j)
    implementation(libs.vanniktech.blurhash)
    implementation(libs.telephoto.zoomableimage)
    implementation(libs.matrix.emojibase.bindings)
    implementation(projects.features.knockrequests.api)
    implementation(projects.features.roommembermoderation.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.features.call.test)
    testImplementation(projects.features.forward.test)
    testImplementation(projects.features.knockrequests.test)
    testImplementation(projects.features.location.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.features.messages.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.mediaupload.impl)
    testImplementation(projects.libraries.mediaupload.test)
    testImplementation(projects.libraries.mediapickers.test)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.voicerecorder.test)
    testImplementation(projects.libraries.mediaplayer.test)
    testImplementation(projects.libraries.mediaviewer.test)
    testImplementation(projects.libraries.testtags)
    testImplementation(projects.features.poll.test)
    testImplementation(projects.libraries.eventformatter.test)
    testImplementation(projects.libraries.recentemojis.test)
}
