package extension

import Versions
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import java.io.File

fun CommonExtension<*, *, *, *>.androidConfig(project: Project) {
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

    lint {
        lintConfig = File("${project.rootDir}/tools/lint/lint.xml")
        checkDependencies = true
        abortOnError = true
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

    lint {
        // Extra rules for compose
        // Disabled until lint stops inspecting generated ksp files...
        // error.add("ComposableLambdaParameterNaming")
        error.add("ComposableLambdaParameterPosition")
    }
}

