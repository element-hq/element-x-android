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

pluginManagement {
    repositories {
        includeBuild("plugins")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libraries/matrix/libs")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ElementX"
include(":app")
include(":libraries:core")
include(":libraries:rustsdk")
include(":libraries:matrix:api")
include(":libraries:matrix:impl")
include(":libraries:matrix:test")
include(":libraries:matrixui")
include(":libraries:textcomposer")
include(":libraries:dateformatter:api")
include(":libraries:dateformatter:impl")
include(":libraries:dateformatter:test")
include(":libraries:elementresources")
include(":libraries:ui-strings")
include(":libraries:testtags")
include(":features:onboarding")
include(":features:login")
include(":features:logout")
include(":features:roomlist")
include(":features:messages")
include(":features:rageshake")
include(":features:preferences")
include(":libraries:designsystem")
include(":libraries:di")
include(":tests:uitests")
include(":anvilannotations")
include(":anvilcodegen")
include(":libraries:architecture")
include(":features:template")
include(":libraries:androidutils")
include(":samples:minimal")
include(":libraries:encrypted-db")
include(":libraries:session-storage")
