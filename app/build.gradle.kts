import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "eu.ottop.yamlauncher"
    compileSdk = 37

    defaultConfig {
        applicationId = "eu.ottop.yamlauncher"
        minSdk = 24
        targetSdk = 37
        versionCode = 17
        versionName = "2.3-fork1"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        // Disable pre-existing API compatibility warnings (BlendMode requires API 29+)
        disable += "NewApi"
        // Allow the build to continue even with lint errors to catch new issues
        abortOnError = false
    }

    // Fork release signing comes from the gitignored signing.properties at the repo root.
    val signing = Properties().apply {
        rootProject.file("signing.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    val releaseStoreFile = signing.getProperty("RELEASE_STORE_FILE")?.let { file(it) }

    signingConfigs {
        if (releaseStoreFile?.exists() == true) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = signing.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = signing.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = signing.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "YAM Launcher Dev")
        }

        release {
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
            isProfileable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", "YAM Launcher")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        aidl = true
        viewBinding = true
        buildConfig = false
        resValues = true
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.biometric.ktx)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
