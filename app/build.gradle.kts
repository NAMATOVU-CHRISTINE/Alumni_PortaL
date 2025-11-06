plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.namatovu.alumniportal"
    compileSdk = 36

    defaultConfig {
    applicationId = "com.namatovu.alumniportal"
        minSdk = 26
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Firebase - Bill of Materials (BOM)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")

    // MVVM & Lifecycle Components - Using explicit versions
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.fragment:fragment:1.7.1")
    
    // Material Design for UI - Using explicit version
    implementation("com.google.android.material:material:1.12.0")
    
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Room Database for Offline Storage
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-guava:$room_version") // For ListenableFuture
    
    // WorkManager for Background Sync
    val work_version = "2.9.0"
    implementation("androidx.work:work-runtime:$work_version")
    // KTX helpers for WorkManager (CoroutineWorker, etc.)
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Web Scraping
    implementation("org.jsoup:jsoup:1.17.2")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Guava dependency for ListenableFuture
    implementation("com.google.guava:guava:33.0.0-android")
    
    // Security - Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
