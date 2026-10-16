plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}
android {
    namespace = "dev.sleepguard"
    compileSdk = 36
    defaultConfig {
        applicationId = "dev.sleepguard"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isIncludeAndroidResources = true }
    val uploadStorePath = providers.environmentVariable("SLEEP_GUARD_UPLOAD_STORE_FILE").orNull
    signingConfigs {
        if (uploadStorePath != null) create("upload") {
            storeFile = file(uploadStorePath)
            storePassword = providers.environmentVariable("SLEEP_GUARD_UPLOAD_PASSWORD").get()
            keyAlias = "sleepguard-upload"
            keyPassword = providers.environmentVariable("SLEEP_GUARD_UPLOAD_PASSWORD").get()
            storeType = "PKCS12"
        }
    }
    buildTypes { release {
        isMinifyEnabled = false
        if (uploadStorePath != null) signingConfig = signingConfigs.getByName("upload")
    } }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
