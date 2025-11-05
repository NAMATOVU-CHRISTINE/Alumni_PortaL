// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    // Kotlin Android plugin (used by app module when adding a Kotlin worker)
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}
