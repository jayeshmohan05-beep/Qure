// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Google Services plugin - required for Firebase
    alias(libs.plugins.google.services) apply false
}