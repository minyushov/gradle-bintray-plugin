package com.minyushov.bintray

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal interface Artifact {
  fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication)
}