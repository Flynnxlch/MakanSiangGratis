    plugins {
        alias(libs.plugins.android.application)
        id ("com.google.gms.google-services")
    }

    android {
        namespace = "com.example.makansianggratis"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.example.makansianggratis"
            minSdk = 24
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
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    repositories {
        google()
        mavenCentral()  // Ensure Maven Central is added
    }

    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)

        implementation ("de.hdodenhof:circleimageview:3.1.0")
        implementation ("com.google.firebase:firebase-database:21.0.0")
        implementation ("com.google.firebase:firebase-auth:23.1.0")
        implementation ("com.google.firebase:firebase-firestore:25.1.1")
        implementation ("io.github.jan-tennert.supabase:storage-kt:3.0.2")
        implementation ("com.squareup.picasso:picasso:2.8")
        implementation ("com.github.dhaval2404:imagepicker:2.1")
        implementation ("com.github.bumptech.glide:glide:4.16.0")
        implementation ("com.squareup.okhttp3:okhttp:4.11.0")
        implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
        implementation ("com.squareup.retrofit2:retrofit:2.9.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation ("commons-io:commons-io:2.11.0")

        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }
