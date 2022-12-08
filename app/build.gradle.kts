plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.7.20-1.0.7"
    id("com.google.firebase.appdistribution") version "3.0.2"
}

android {
    namespace = "io.element.android.x"
    compileSdk = 33

    defaultConfig {
        applicationId = "io.element.android.x"
        minSdk = 29
        targetSdk = 33
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
    implementation("io.github.raamcosta.compose-destinations:animations-core:1.7.23-beta")
    ksp("io.github.raamcosta.compose-destinations:ksp:1.7.23-beta")

    val composeBom = platform("androidx.compose:compose-bom:2022.11.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("io.coil-kt:coil:2.2.1")
    implementation("com.jakewharton.timber:timber:5.0.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.airbnb.android:mavericks-compose:3.0.1")

    implementation("com.airbnb.android:showkase:1.0.0-beta14")
    ksp("com.airbnb.android:showkase-processor:1.0.0-beta14")
}