import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}


android {
    namespace = "com.example.app_buladigital"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app_buladigital"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // O buildFeatures fica aqui, no nível da raiz do bloco android
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("keystore.path") ?: "")
            storePassword = localProperties.getProperty("keystore.password")
            keyAlias = localProperties.getProperty("key.alias")
            keyPassword = localProperties.getProperty("key.password")
        }
    }

    buildTypes {
        // 2. Usamos getByName para configurar os tipos existentes
        getByName("debug") {
            buildConfigField("String", "BASE_URL", "\"https://apibuladigital.incubadorascriativas.org.br/\"")
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://apibuladigital.incubadorascriativas.org.br/\"")

            isMinifyEnabled = false // Mantemos falso por enquanto para evitar bugs
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Dependências para requisições HTTP e processamento JSON
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.google.code.gson:gson:2.13.1")

    // Dependências para Coroutines, que facilitam tarefas assíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")

    implementation("androidx.cardview:cardview:1.0.0")

    implementation("com.google.android.material:material:1.11.0")

    //Imagens vindas do html
    implementation("io.coil-kt:coil:2.5.0")

    implementation("androidx.fragment:fragment-ktx:1.8.5")

}