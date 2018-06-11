package com.minyushov.bintray

import org.gradle.api.Project
import java.io.DataInputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.Properties

private const val PLUGIN_JAVA = "java"
private const val PLUGIN_JAVA_LIBRARY = "java-library"
private const val PLUGIN_KOTLIN = "kotlin"
private const val PLUGIN_KOTLIN_ANDROID = "kotlin-android"
private const val PLUGIN_ANDROID_LIBRARY = "com.android.library"

private const val PROPERTIES_NAME = "local.properties"

internal fun Project.getType() =
  project.pluginManager.run {
    when {
      hasPlugin(PLUGIN_ANDROID_LIBRARY) && hasPlugin(PLUGIN_KOTLIN_ANDROID) -> ProjectType.KotlinAndroid()
      hasPlugin(PLUGIN_ANDROID_LIBRARY) -> ProjectType.JavaAndroid()
      hasPlugin(PLUGIN_KOTLIN) -> ProjectType.Kotlin()
      hasPlugin(PLUGIN_JAVA_LIBRARY) || hasPlugin(PLUGIN_JAVA) -> ProjectType.Java()
      else -> ProjectType.Unknown()
    }
  }

internal fun Project.localProperty(key: String): String? =
  localProperties(PROPERTIES_NAME) { getProperty(key) }
      ?: rootProject.localProperties(PROPERTIES_NAME) { getProperty(key) }

private inline fun Project.localProperties(name: String, action: Properties.() -> String?): String? =
  try {
    file(name).dataInputStream().use {
      action(Properties().apply { load(it) })
    }
  } catch (exception: FileNotFoundException) {
    null
  }

private fun File.dataInputStream() =
  DataInputStream(inputStream())
