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
    id("io.element.android-library")
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "io.element.android.libraries.sessionstorage.impl"
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.encryptedDb)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.sqldelight.driver.android)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite)
    implementation(projects.libraries.di)
    implementation(libs.sqldelight.coroutines)

    testCommonDependencies(libs)
    testImplementation(libs.sqldelight.driver.jvm)
}

sqldelight {
    databases {
        create("SessionDatabase") {
            // https://sqldelight.github.io/sqldelight/2.1.0/android_sqlite/migrations/
            // To generate a .db file from your latest schema, run this task
            // ./gradlew generateDebugSessionDatabaseSchema
            // Test migration by running
            // ./gradlew verifySqlDelightMigration
            schemaOutputDirectory = File("src/main/sqldelight/databases")
            verifyMigrations = true
        }
    }
}
