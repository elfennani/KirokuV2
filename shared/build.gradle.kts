import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.apollographql.apollo") version "5.0.0-alpha.5"
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("co.touchlab.skie") version "0.10.10"
    alias(libs.plugins.build.konfig)
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
            isStatic = false
            linkerOpts.add("-lsqlite3")
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation("com.apollographql.apollo:apollo-runtime:5.0.0-alpha.5")
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.2.0-RC1"))
            implementation("io.insert-koin:koin-core")
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Serialization
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)

        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation("io.insert-koin:koin-android")
        }
        macosMain.dependencies {
            implementation(libs.ktor.client.darwin)
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

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspMacosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
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

fun readTokenProperties(): Map<String, String> {
    val items = HashMap<String, String>()

    val fl = rootProject.file("token.properties")

    if (fl.exists()) {
        fl.forEachLine {
            items[it.split("=")[0]] = it.split("=")[1]
        }
    }

    return items
}

val tokenProperties = readTokenProperties()

buildkonfig {
    packageName = "com.elfennani.kiroku"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "CLIENT_ID", tokenProperties["CLIENT_ID"])
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", "true")
    }

    defaultConfigs("debug") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", "true")
    }

    defaultConfigs("release") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "IS_DEBUG", "false")
    }
}