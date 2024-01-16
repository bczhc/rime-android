buildscript {
    dependencies {
        classpath("com.github.bczhc:android-native-build-plugin:c546ada670")
    }
}

plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.7.21" apply false
}
