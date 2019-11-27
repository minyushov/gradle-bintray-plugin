package com.minyushov.bintray.project

import com.minyushov.bintray.BintrayExtension
import org.gradle.api.Project
import org.jetbrains.dokka.gradle.DokkaPlugin

internal class KotlinLibraryProject(
  project: Project,
  extension: BintrayExtension
) : BintrayProject(
  project,
  extension
) {

  override fun applyPlugins() {
    super.applyPlugins()
    project.pluginManager.apply(DokkaPlugin::class.java)
  }

  override val components
    get() = listOf(
      componentProvider.jar
    )

  override val artifacts
    get() = listOfNotNull(
      if (extension.docs.getOrElse(true)) {
        artifactProvider.dokka
      } else null,
      if (extension.sources.getOrElse(true)) {
        artifactProvider.javaSources
      } else null
    )
}