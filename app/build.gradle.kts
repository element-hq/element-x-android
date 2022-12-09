plugins {
    id("io.element.android-compose-application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    id("com.google.firebase.appdistribution") version "3.0.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
}

android {
    namespace = "io.element.android.x"

    defaultConfig {
        applicationId = "io.element.android.x"
        targetSdk = 33 // TODO Use Versions.targetSdk
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
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
            resValue("string", "app_name", "ElementX dbg")
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        named("release") {
            resValue("string", "app_name", "ElementX")
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
            resValue("string", "app_name", "ElementX nightly")
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("nightly")

            postprocessing {
                initWith(release.postprocessing)
            }

            firebaseAppDistribution {
                artifactType = "APK"
                // releaseNotesFile = TODO
                groups = "external-testers"
                // This should not be required, but if I do not add the appId, I get this error:
                // "App Distribution halted because it had a problem uploading the APK: [404] Requested entity was not found."
                appId = "1:912726360885:android:e17435e0beb0303000427c"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kotlin {
        sourceSets.main {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
        sourceSets.test {
            kotlin.srcDir("build/generated/ksp/test/kotlin")
        }
    }

    // See https://github.com/JLLeitschuh/ktlint-gradle#configuration
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        // See https://github.com/pinterest/ktlint/releases/
        // TODO 0.47.1 is available
        version.set("0.45.1")
        android.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        // display the corresponding rule
        verbose.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            // To have XML report for Danger
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        filter {
            exclude { element -> element.file.path.contains("$buildDir/generated/") }
        }
        disabledRules.set(
            setOf(
                // TODO Re-enable these 4 rules after reformatting project
                "indent",
                "experimental:argument-list-wrapping",
                "max-line-length",
                "parameter-list-wrapping",

                "spacing-between-declarations-with-comments",
                "no-multi-spaces",
                "experimental:spacing-between-declarations-with-annotations",
                "experimental:annotation",
                // - Missing newline after "("
                // - Missing newline before ")"
                "wrapping",
                // - Unnecessary trailing comma before ")"
                "experimental:trailing-comma",
                // - A block comment in between other elements on the same line is disallowed
                "experimental:comment-wrapping",
                // - A KDoc comment after any other element on the same line must be separated by a new line
                "experimental:kdoc-wrapping",
                // Ignore error "Redundant curly braces", since we use it to fix false positives, for instance in "elementLogs.${i}.txt"
                "string-template",
            )
        )
    }
}

allprojects {
    detekt {
        // preconfigure defaults
        buildUponDefaultConfig = true
        // activate all available (even unstable) rules.
        allRules = true
        // point to your custom config defining rules to run, overwriting default behavior
        config = files("$rootDir/tools/detekt/detekt.yml")
    }
}

dependencies {
    implementation(project(":libraries:designsystem"))
    implementation(project(":libraries:matrix"))
    implementation(project(":libraries:core"))
    implementation(project(":features:onboarding"))
    implementation(project(":features:login"))
    implementation(project(":features:roomlist"))
    implementation(project(":features:messages"))

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.0")
    implementation(libs.compose.destinations)
    ksp(libs.compose.destinations.processor)

    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil)
    implementation(libs.mavericks.compose)

    implementation(libs.showkase)
    ksp(libs.showkase.processor)
}
