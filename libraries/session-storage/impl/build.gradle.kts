import extension.setupAnvil

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "io.element.android.libraries.sessionstorage.impl"
}

setupAnvil()

dependencies {
    implementation(libs.dagger)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.encryptedDb)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.sqldelight.driver.android)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite)
    implementation(projects.libraries.di)
    implementation(libs.sqldelight.coroutines)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.sqldelight.driver.jvm)
    testImplementation(projects.tests.testutils)
}

sqldelight {
    databases {
        create("SessionDatabase") {
            // https://cashapp.github.io/sqldelight/2.0.0/android_sqlite/migrations/
            // To generate a .db file from your latest schema, run this task
            // ./gradlew generateDebugSessionDatabaseSchema
            // Test migration by running
            // ./gradlew verifySqlDelightMigration
            schemaOutputDirectory = File("src/main/sqldelight/databases")
            verifyMigrations = true
        }
    }
}
