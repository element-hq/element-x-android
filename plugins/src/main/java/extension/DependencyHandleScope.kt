package extension

import gradle.kotlin.dsl.accessors._4b7ad2363fc1fce7c774e054dc9a9300.androidTestImplementation
import gradle.kotlin.dsl.accessors._4b7ad2363fc1fce7c774e054dc9a9300.debugImplementation
import gradle.kotlin.dsl.accessors._4b7ad2363fc1fce7c774e054dc9a9300.implementation
import org.gradle.kotlin.dsl.DependencyHandlerScope


fun DependencyHandlerScope.composeDependencies(){
    val composeBom = platform("androidx.compose:compose-bom:2022.11.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("com.airbnb.android:mavericks-compose:3.0.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.airbnb.android:showkase:1.0.0-beta14")
}

