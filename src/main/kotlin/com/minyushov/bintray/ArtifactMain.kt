package com.minyushov.bintray

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal class ArtifactMainJava : Artifact {
  override fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) =
    publication.from(
      project.components.findByName("java")
        ?: throw GradleException("Unable to find 'java' component")
    )
}

internal class ArtifactMainAndroid : Artifact {
  override fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) =
    publication.from(
      project.components.findByName(extension.variant.get())
        ?: throw GradleException("Unable to find 'android' component")
    )
}