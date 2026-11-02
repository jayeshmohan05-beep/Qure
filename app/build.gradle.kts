plugins {
    alias(libs.plugins.android.application)
    // Apply Google Services plugin - this reads the google-services.json file
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.qure"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.qure"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)

    // RecyclerView - for displaying lists (patient queue, appointments, etc.)
    implementation(libs.recyclerview)

    // CardView - for card-style UI components
    implementation(libs.cardview)

    // Firebase - BOM manages all Firebase library versions automatically
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)        // Login / Register
    implementation(libs.firebase.firestore)   // Cloud database
    implementation(libs.firebase.storage)     // File/image upload

    // Google Maps and Location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Glide - for loading images from Firebase Storage URLs
    implementation(libs.glide)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}