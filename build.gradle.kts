buildscript {
    dependencies {
        classpath(files("/home/bczhc/code/android-native-build-plugin/build/libs/android-native-build-plugin-all.jar"))
    }
}

plugins {
    id("com.android.application") version "7.3.0" apply false
    id("com.android.library") version "7.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.21" apply false
}
