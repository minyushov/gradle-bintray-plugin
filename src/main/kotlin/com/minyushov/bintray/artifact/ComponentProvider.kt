package com.minyushov.bintray.artifact

import com.minyushov.bintray.BintrayExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent

internal class ComponentProvider(
  private val project: Project,
  private val extension: BintrayExtension
) {

  val jar: SoftwareComponent
    get() = project.components.findByName("java")
      ?: throw GradleException("Unable to find 'java' component")

  val aar: SoftwareComponent
    get() = project.components.findByName(extension.variant.get())
      ?: throw GradleException("Unable to find 'android' component")

}