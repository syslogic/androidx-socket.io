import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    kotlin("android")
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.navigation.safeargs)
}

android {
    namespace = "io.syslogic.socketio"
    buildToolsVersion = libs.versions.android.buildTools.get()
    compileSdk = Integer.parseInt(libs.versions.android.compileSdk.get())

    defaultConfig {
        applicationId = "io.syslogic.socketio"
        versionName = libs.versions.app.versionName.get()
        versionCode = Integer.parseInt(libs.versions.app.versionCode.get())
        targetSdk = Integer.parseInt(libs.versions.android.targetSdk.get())
        minSdk = Integer.parseInt(libs.versions.android.minSdk.get())
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isJniDebuggable = true
            isPseudoLocalesEnabled = false
            enableAndroidTestCoverage = true
            isShrinkResources = false
            isMinifyEnabled = false
        }
        named("release") {
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            enableAndroidTestCoverage = false
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFile("default.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.socket.io.client) {
        // exclude(group = "org.json", module = "json")
    }

    implementation(libs.material.design)
    implementation(libs.bundles.androidx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.androidx.test)
    debugImplementation(libs.bundles.androidx.testing)
}
