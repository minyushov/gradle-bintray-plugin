package com.minyushov.bintray.project

import com.minyushov.bintray.BintrayExtension
import org.gradle.api.Project

internal class JavaLibraryProject(
  project: Project,
  extension: BintrayExtension
) : BintrayProject(
  project,
  extension
) {

  override val components
    get() = listOf(
      componentProvider.jar
    )

  override val artifacts
    get() = listOfNotNull(
      if (extension.docs.getOrElse(true)) {
        artifactProvider.javadoc
      } else null,
      if (extension.sources.getOrElse(true)) {
        artifactProvider.javaSources
      } else null
    )
}