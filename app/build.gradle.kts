plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.namatovu.alumniportal"
    compileSdk = 36

    defaultConfig {
    applicationId = "com.namatovu.alumniportal"
        minSdk = 23
        targetSdk = 36
        versionCode = 9
        versionName = "1.3.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Support for 16 KB page sizes (required for Android 15+)
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            // Reduce debug symbols for smaller size
            debugSymbolLevel = "FULL"
        }
        
        // Additional vector drawable support
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Generate native debug symbols
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
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
    
    lint {
        disable.add("MissingPermission")
        disable.add("MissingTranslation")
        disable.add("ExtraTranslation")
    }
    
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/proguard/androidx-*.pro",
                "META-INF/com.android.tools/**",
                "DebugProbesKt.bin"
            )
        }
        
        // Enable 16 KB page size alignment for native libraries (Android 15+)
        jniLibs {
            useLegacyPackaging = false
        }
    }
    
    // Additional configuration for 16 KB page size support
    androidResources {
        noCompress += listOf("tflite", "lite")
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
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

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
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-guava:$room_version") // For ListenableFuture
    
    // WorkManager for Background Sync
    val work_version = "2.9.0"
    implementation("androidx.work:work-runtime:$work_version")
    // KTX helpers for WorkManager (CoroutineWorker, etc.)
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Image Loading - Updated to latest version with 16KB support
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    
    // Cloudinary for image storage - Exclude Fresco completely
    implementation("com.cloudinary:cloudinary-android:2.5.0") {
        exclude(group = "com.facebook.fresco")
    }

    // Web Scraping
    implementation("org.jsoup:jsoup:1.17.2")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Guava dependency for ListenableFuture
    implementation("com.google.guava:guava:33.0.0-android")
    
    // Security - Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Gmail API for sending emails
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-gmail:v1-rev20220404-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.3")
    
    // JavaMail for email creation
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
}
