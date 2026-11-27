/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

rootProject.name = "ElementX_plugins"

dependencyResolutionManagement {
    repositories {
        // Declared first and filtered so that the Android Gradle Plugin and its many dependencies are not looked up in
        // Maven Central first, and the other dependencies are not looked up in Google's Maven repository.
        google {
            content {
                includeGroupByRegex("android\\..*")
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*)?")
                includeGroupByRegex("com\\.google\\.firebase.*")
                includeGroupByRegex("com\\.google\\.android.*")
                includeGroupByRegex("com\\.google\\.gms.*")
                includeGroupByRegex("com\\.google\\.testing\\.platform.*")
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
