plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.sentinelonex"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.sentinelonex"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}
dependencies {
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
