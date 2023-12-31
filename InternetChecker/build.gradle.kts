plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.example.internetchecker"
    compileSdk = 33

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }




    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
}



configure<PublishingExtension> {
    publications.create<MavenPublication>("release") {
        groupId = "com.github.abdelrhman"
        artifactId = "connectivity-manager"
        version = android.defaultConfig.versionName
        pom.packaging = "jar"
        artifact("$buildDir/outputs/aar/InternetChecker-release.aar")

        pom {
            packaging = "aar"
            // Add any other necessary information to the POM file
        }

    }
    repositories {
        // Use JitPack or other remote repository URL here
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        // Other repositories if needed...
    }
}