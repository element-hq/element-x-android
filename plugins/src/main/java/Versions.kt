import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.jvm.toolchain.JavaLanguageVersion

val VersionCatalog.composeVersion: String
    get() = findVersion("compose_compiler").get().requiredVersion

object Versions {
    const val versionCode = 100100
    const val versionName = "0.1.0"

    const val compileSdk = 33
    const val targetSdk = 33
    const val minSdk = 21
    val javaCompileVersion = JavaVersion.VERSION_11
    val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(11)
}
