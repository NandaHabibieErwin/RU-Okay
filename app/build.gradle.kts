plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.blackjack.ru_okay"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blackjack.ru_okay"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation("com.sendbird.sdk:uikit:3.+")
    implementation ("com.google.firebase:firebase-storage:21.0.0")
    implementation ("com.github.AgoraIO-Community.VideoUIKit-Android:final:4.0.1")
    implementation ("io.agora.rtc:full-sdk:4.0.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation(libs.firebase.vertexai)
    implementation(libs.billing)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}