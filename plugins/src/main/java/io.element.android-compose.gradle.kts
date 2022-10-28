import extension.androidConfig
import extension.composeConfig
import extension.proguardConfig

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    androidConfig()
    proguardConfig()
    composeConfig()
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2022.10.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("com.airbnb.android:mavericks-compose:3.0.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

}
