plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")


}

android {
        signingConfigs {
        getByName("debug") {
            keyAlias = "alawraq"
            keyPassword = "alawraq"
            storePassword = "alawraq"
            storeFile =
                file("E:\\Developed Apps\\EmojiKitchenApp-master\\alawraqkeystore\\alawraq.jks")
        }
        create("release") {
            storeFile =
                file("E:\\Developed Apps\\EmojiKitchenApp-master\\alawraqkeystore\\alawraq.jks")
            storePassword = "alawraq"
            keyPassword = "alawraq"
            keyAlias = "alawraq"
        }
    }
    namespace = "com.emojimerger.mixemojis.emojifun"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.emojimerger.mixemojis.emojifun"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        setProperty("archivesBaseName", "Alawraq-EmojiMixer-$versionName")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    flavorDimensions += listOf("defaultFlavour")
    productFlavors {
        create("Prod") {
            dimension = "defaultFlavour"
            resValue("string", "admob_app_id", "\"ca-app-pub-7663966277943676~5187241900\"")
//            buildConfigField("String", "interstitial", "\"ca-app-pub-5236996920514682/5811910511\"")
//            buildConfigField("String", "banner", "\"ca-app-pub-5236996920514682/2891105840\"")
//            buildConfigField("String", "native_ad", "\"ca-app-pub-5236996920514682/1054407857\"")
//            buildConfigField("String", "app_open_id", "\"ca-app-pub-5236996920514682/6541704104\"")

            buildConfigField("String","welcome_screen_native","\"ca-app-pub-7663966277943676/5210743895\"")
            buildConfigField("String","home_screen_native","\"ca-app-pub-7663966277943676/8958417214\"")
            buildConfigField("String","home_inters","\"ca-app-pub-7663966277943676/2418480479\"")
            buildConfigField("String","welcome_inters","\"ca-app-pub-7663966277943676/5591438722\"")
            buildConfigField("String","splash_inters","\"ca-app-pub-7663966277943676/7451315300\"")
            buildConfigField("String","mix_emoji_banner","\"ca-app-pub-7663966277943676/4636028824\"")
            buildConfigField("String","collection_activity_banner","\"ca-app-pub-7663966277943676/1239709291\"")
            buildConfigField("String","create_gif_dilog_native","\"ca-app-pub-7663966277943676/7421974265\"")
            buildConfigField("String","new_emoji_native","\"ca-app-pub-7663966277943676/1978075898\"")
            buildConfigField("String","setting_native","\"ca-app-pub-7663966277943676/4221095858\"")
            buildConfigField("String","emoji_loading_dilog_native","\"ca-app-pub-7663966277943676/1403360821\"")
            buildConfigField("String","my_creation_banner","\"ca-app-pub-7663966277943676/1211789131\"")
            buildConfigField("String","my_gif_screen_bannner","\"ca-app-pub-7663966277943676/3646380789\"")
            buildConfigField("String","splash_app_openn","\"ca-app-pub-7663966277943676/6955070409\"")

            buildConfigField("String","app_open","\"ca-app-pub-7663966277943676/3562620308\"")


            buildConfigField ("Boolean", "env_dev", "false")

        }
        create("Dev") {
            dimension = "defaultFlavour"

            resValue("string", "admob_app_id", "\"ca-app-pub-3940256099942544~3347511713\"")
//            buildConfigField("String", "interstitial", "\"ca-app-pub-3940256099942544/1033173712\"")
//            buildConfigField("String", "banner", "\"ca-app-pub-3940256099942544/6300978111\"")
//            buildConfigField("String", "native_ad", "\"ca-app-pub-3940256099942544/2247696110\"")
//            buildConfigField("String", "app_open_id", "\"ca-app-pub-3940256099942544/3419835294\"")

            buildConfigField("String", "home_inters", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "welcome_inters", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "splash_inters", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "mix_emoji_banner", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "collection_activity_banner", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "my_creation_banner", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "my_gif_screen_bannner", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "welcome_screen_native", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "home_screen_native", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "create_gif_dilog_native", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "new_emoji_native", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "setting_native", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "emoji_loading_dilog_native", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "splash_app_openn", "\"ca-app-pub-3940256099942544/3419835294\"")


            buildConfigField("String", "app_open", "\"ca-app-pub-3940256099942544/3419835294\"")

            buildConfigField ("Boolean", "env_dev", "true")

        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //libraries for networkcall and gson converter
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
    implementation("com.google.code.gson:gson:2.10")
    implementation("androidx.palette:palette:1.0.0")

    //glide library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.picasso:picasso:2.8")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    //sdp and ssp library
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    //conastraint layout
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha13")

    //paperDB
    implementation("io.github.pilgr:paperdb:2.7.2")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.4.1"))

    //firebase crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    //viewmodel and livedata dependencies
    val lifecycle_version = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    //strokedText
    implementation("com.github.iamBedant:OutlineTextView:1.0.5")
    //lottieAnimation
    implementation("com.airbnb.android:lottie:6.1.0")
    //gif implementation
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.23")

    //ads implementation
    implementation ("apero-inhouse:apero-ads:1.10.0-snapshot14")
    //admob gdpr
    implementation ("com.google.android.ump:user-messaging-platform:2.1.0")



}