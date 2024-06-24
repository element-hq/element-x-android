import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask

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

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(libs.ksp.plugin)
    implementation(projects.anvilannotations)
    api(libs.anvil.compiler.api)
    implementation(libs.anvil.compiler.utils)
    implementation(libs.kotlinpoet)
    implementation(libs.dagger)
    compileOnly(libs.google.autoservice.annotations)
    kapt(libs.google.autoservice)
    implementation(libs.ksp.plugin)
    implementation("com.squareup:kotlinpoet-ksp:1.17.0")
}

tasks.withType<KaptGenerateStubsTask>().configureEach {
    // TODO necessary until anvil supports something for K2 contribution merging
    compilerOptions {
        progressiveMode.set(false)
        languageVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}
