
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.appmusic_basico"
    // Es buena práctica usar variables de la SDK, pero 36 es válido si ya está instalado.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.appmusic_basico"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔹 Spotify Web API redirect URI (HTTPS)
        manifestPlaceholders["redirectSchemeName"] = "https"
        manifestPlaceholders["redirectHostName"] = "appmusic-web-auth"
        manifestPlaceholders["redirectPathPattern"] = "/spotify-callback"

        // 🔹 Spotify App Remote redirect URI (custom scheme)
        manifestPlaceholders["redirectSchemeAppRemote"] = "spotify-auth-app-basico"
        manifestPlaceholders["redirectHostAppRemote"] = "callback"

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

}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ CORRECCIÓN 1: Sintaxis correct para incluir el AAR local
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))

    // ✅ CORRECCIÓN 2: Dependencia Spotify Auth (la correcta de Maven)
    implementation("com.spotify.android:auth:2.1.0")

    // ✅ SOLUCIÓN AL FATAL EXCEPTION: Gson es necesario por Spotify App Remote
    implementation("com.google.code.gson:gson:2.10.1")

    // Dependencias de imágenes y red
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.browser:browser:1.9.0") // Versión estable recomendada
    // Correct for a .kts file
    implementation("com.google.android.material:material:1.11.0")
}
