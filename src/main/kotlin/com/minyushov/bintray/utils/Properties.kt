package com.minyushov.bintray.utils

import org.gradle.api.Project
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.util.Properties

private const val PROPERTIES_NAME = "local.properties"

internal fun Project.localProperty(key: String): String? =
  localProperties(PROPERTIES_NAME) { getProperty(key) }
    ?: rootProject.localProperties(PROPERTIES_NAME) { getProperty(key) }

private fun Project.localProperties(name: String, action: Properties.() -> String?): String? =
  try {
    DataInputStream(file(name).inputStream()).use {
      action(Properties().apply { load(it) })
    }
  } catch (exception: FileNotFoundException) {
    null
  }