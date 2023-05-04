@file:Suppress("UnstableApiUsage")

import org.apache.commons.io.FileUtils
import pers.zhc.gradle.plugins.ndk.rust.RustBuildPlugin
import pers.zhc.gradle.plugins.ndk.rust.RustBuildPlugin.RustBuildPluginExtension
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

apply<RustBuildPlugin>()

android {
    namespace = "pers.zhc.android.rime"
    compileSdk = 33

    defaultConfig {
        applicationId = "pers.zhc.android.rime"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        val types = asMap
        types["debug"]!!.apply {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            isJniDebuggable = true
            signingConfig = signingConfigs["debug"]
        }
        types["release"]!!.apply {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            isDebuggable = true
            isJniDebuggable = true
            signingConfig = signingConfigs["debug"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        val sets = asMap
        sets["main"]!!.apply {
            jniLibs.srcDirs("jniLibs")
        }
    }
}

val configFile = File(rootProject.projectDir, "config.properties")
if (!configFile.exists()) {
    throw GradleException("config.properties not exists")
}
val configs = Properties().apply {
    load(configFile.reader())
}

val ndkTargets = (configs["ndk.targets"] ?: throw GradleException("ndk.targets missing")) as String
val ndkTargetsConfig = ndkTargets.split(',').map {
    val groupValues = Regex("^(.*)-([0-9]+)\$").findAll(it).first().groupValues
    mapOf(
        Pair("abi", groupValues[1]),
        Pair("api", groupValues[2].toInt())
    )
}
val abis = ndkTargetsConfig.map { it["abi"]!! as String }

val jniOutputDir = file("jniLibs").also { it.mkdir() }
val librimeLibDir = (configs["librime-lib-dir"] ?: throw GradleException("librime-lib-dir missing")) as String
val librimeIncludeDir =
    (configs["librime-include-dir"] ?: throw GradleException("librime-include-dir missing")) as String

val rustBuildTargetEnv = HashMap<String, Map<String, String>>()
abis.forEach { abi ->
    rustBuildTargetEnv[abi] = mapOf(
        Pair("RIME_LIB_DIR", File(librimeLibDir, abi).path),
        Pair("RIME_INCLUDE_DIR", librimeIncludeDir)
    )
}

configure<RustBuildPluginExtension> {
    srcDir.set("$projectDir/src/main/rust")
    ndkDir.set(android.ndkDirectory.path)
    targets.set(ndkTargetsConfig)
    buildType.set("release")
    outputDir.set(jniOutputDir.path)
    targetEnv.set(rustBuildTargetEnv)
}

val compileRustTask = tasks.findByName(RustBuildPlugin.TASK_NAME())!!
val copyLibrimeTask = task("copyLibrimeTask") {
    doLast {
        val name = "librime.so"
        abis.forEach { abi ->
            FileUtils.copyFile(FileUtils.getFile(librimeLibDir, abi, name), FileUtils.getFile(jniOutputDir, abi, name))
        }
    }
}

val compileJniTask = task("compileJni") {
    dependsOn(compileRustTask)
    dependsOn(copyLibrimeTask)
}

project.tasks.getByName("preBuild").dependsOn(compileJniTask)

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("com.google.code.gson:gson:2.10.1")
}
