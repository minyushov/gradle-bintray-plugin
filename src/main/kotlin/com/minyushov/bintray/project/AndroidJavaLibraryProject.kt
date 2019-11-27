package com.minyushov.bintray.project

import com.minyushov.bintray.BintrayExtension
import org.gradle.api.Project

internal class AndroidJavaLibraryProject(
  project: Project,
  extension: BintrayExtension
) : BintrayProject(
  project,
  extension
) {

  override val components
    get() = listOf(
      componentProvider.aar
    )

  override val artifacts
    get() = listOfNotNull(
      if (extension.docs.getOrElse(true)) {
        artifactProvider.javadocAndroid
      } else null,
      if (extension.sources.getOrElse(true)) {
        artifactProvider.androidSources
      } else null
    )

}