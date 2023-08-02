// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val agpVersion = "8.1.0"
    val kotlinVersion = "1.8.21"
    id("com.android.application").version(agpVersion).apply(false)
    id("com.android.library").version(agpVersion).apply(false)
    id("org.jetbrains.kotlin.android").version(kotlinVersion).apply(false)
    id("org.jetbrains.kotlin.plugin.serialization").version(kotlinVersion).apply(false)
}