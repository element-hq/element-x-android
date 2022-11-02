package extension

import Versions
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import composeVersion
import org.gradle.api.artifacts.VersionCatalog

fun CommonExtension<*, *, *, *>.androidConfig() {
    defaultConfig {
        compileSdk = Versions.compileSdk
        minSdk = Versions.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

fun CommonExtension<*, *, *, *>.composeConfig() {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    packagingOptions {
        resources.excludes.apply {
            add("META-INF/AL2.0")
            add("META-INF/LGPL2.1")
        }
    }
}

fun LibraryExtension.proguardConfig() {
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

