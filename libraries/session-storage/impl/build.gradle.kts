/*
 * Copyright (c) 2023 New Vector Ltd
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
plugins {
    id("io.element.android-library")
    alias(libs.plugins.anvil)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "io.element.android.libraries.sessionstorage.impl"
}

anvil {
    useKsp(
        contributesAndFactoryGeneration = true,
        componentMerging = true,
    )
//    generateDaggerFactories = true
}

dependencies {
    implementation(libs.dagger)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.encryptedDb)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.sqldelight.driver.android)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite)
    implementation(libs.androidx.security.crypto)
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
