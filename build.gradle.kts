
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false   // 👈 add this
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
}