package com.minyushov.bintray

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import java.io.DataInputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.Properties

private const val PLUGIN_JAVA = "java-library"
private const val PLUGIN_KOTLIN = "kotlin"
private const val PLUGIN_KOTLIN_ANDROID = "kotlin-android"
private const val PLUGIN_ANDROID_LIBRARY = "com.android.library"

private const val PROPERTIES_NAME = "local.properties"

internal fun Project.configure(extension: BintraySimpleExtension) {
  getType()
    .takeIf { it !is ProjectType.UnknownProject }
    ?.run {
      pluginsConfigurators.forEach { it.configure(project) }

      extension.version?.let { project.setProperty("version", it) }
      extension.groupId?.let { project.setProperty("group", it) }
      extension.artifactId?.let { project.setProperty("archivesBaseName", it) }

      val publishing = project.extensions.getByType(PublishingExtension::class.java)
          ?: throw GradleException("Unable to find publishing extension")

      publishing.publications.create("maven", MavenPublication::class.java) { publication ->

        publication.apply {
          artifactId = extension.artifactId
          groupId = extension.groupId
          version = extension.version
        }

        artifacts.forEach {
          it.apply(project, extension, publication)
        }
      }

      bintrayConfigurator.configure(project, extension, publishing.publications.mapNotNull { it.name })
    }
}

private fun Project.getType() =
  project.pluginManager.run {
    when {
      hasPlugin(PLUGIN_ANDROID_LIBRARY) && hasPlugin(PLUGIN_KOTLIN_ANDROID) -> ProjectType.KotlinAndroidProject()
      hasPlugin(PLUGIN_ANDROID_LIBRARY) -> ProjectType.JavaAndroidProject()
      hasPlugin(PLUGIN_KOTLIN) -> ProjectType.KotlinProject()
      hasPlugin(PLUGIN_JAVA) -> ProjectType.JavaProject()
      else -> ProjectType.UnknownProject()
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
