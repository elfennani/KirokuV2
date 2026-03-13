import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.apollographql.apollo") version "5.0.0-alpha.5"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    macosArm64(){
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation("com.apollographql.apollo:apollo-runtime:5.0.0-alpha.5")
        }
        macosMain.dependencies {

        }
    }
}

android {
    namespace = "com.elfennani.kiroku.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

apollo {
    service("anilist") {
        packageName.set("com.elfennani.shared.anilist")
        srcDir("src/commonMain/graphqlAnilist")
        includes.add("**/*.graphql")

        introspection {
            endpointUrl.set("https://graphql.anilist.co")
            schemaFile.set(file("src/commonMain/graphqlAnilist/anilist_schema.json"))
        }
    }
    service("allanime") {
        packageName.set("com.elfennani.shared.allanime")
        srcDir("src/commonMain/graphqlAllAnime")
        introspection {
            endpointUrl.set("https://api.allanime.day/api")
            schemaFile.set(file("src/commonMain/graphqlAllAnime/allanime_schema.json"))
        }
    }
}