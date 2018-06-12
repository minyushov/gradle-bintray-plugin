package com.minyushov.bintray

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal abstract class ArtifactMain : Artifact {
  override fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) =
    apply(project, publication)

  abstract fun apply(project: Project, publication: MavenPublication)
}

internal class ArtifactMainJava : ArtifactMain() {
  override fun apply(project: Project, publication: MavenPublication) =
    publication.from(
      project.components.findByName("java")
          ?: throw GradleException("Unable to find 'java' component")
    )
}

internal class ArtifactMainAndroid : ArtifactMain() {
  override fun apply(project: Project, publication: MavenPublication) =
    publication.from(
      project.components.findByName("android")
          ?: throw GradleException("Unable to find 'android' component")
    )
}