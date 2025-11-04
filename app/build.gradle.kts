plugins {
    alias(libs.plugins.android.application)
}

// 🚀 CORRECCIÓN CLAVE: Usamos 'val' de Kotlin DSL
val glideVersion = "4.16.0"

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

    // ✅ AAR local
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))

    // ✅ Dependencia Spotify Auth
    implementation("com.spotify.android:auth:2.1.0")

    // ✅ Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Dependencias de imágenes y red
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.browser:browser:1.9.0")
    implementation("com.google.android.material:material:1.11.0")

    // 🚀 DEPENDENCIAS DE GLIDE LIMPIAS Y CON LA VARIABLE CORREGIDA
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    annotationProcessor("com.github.bumptech.glide:compiler:$glideVersion")
    implementation("com.github.bumptech.glide:annotations:$glideVersion")
    implementation("com.github.bumptech.glide:okhttp3-integration:$glideVersion")

}
