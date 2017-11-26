package com.minyushov.bintray

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class BintrayPlugin : Plugin<Project> {
  companion object {
    private const val EXTENSION_NAME = "bintrayUpload"
    internal const val DOCUMENTATION_CONFIGURATION = "documentation"
  }

  override fun apply(project: Project) {
    val extension = project.extensions.create(EXTENSION_NAME, BintraySimpleExtension::class.java, project)

    project.configurations.apply {
      if (findByName(BintrayPlugin.DOCUMENTATION_CONFIGURATION) != null) {
        throw GradleException("Configuration '${BintrayPlugin.DOCUMENTATION_CONFIGURATION}' is already defined")
      }
      create(BintrayPlugin.DOCUMENTATION_CONFIGURATION)
    }

    project.afterEvaluate {
      if (extension.user == null) {
        extension.user = project.localProperty("bintray.user")
      }
      if (extension.key == null) {
        extension.key = project.localProperty("bintray.key")
      }
      validateExtension(extension)
      project.configure(extension)
    }
  }

  private fun validateExtension(extension: BintraySimpleExtension) {
    extension.user ?: throw GradleException("Bintray user is not defined in '$EXTENSION_NAME' extension")
    extension.key ?: throw GradleException("Bintray api key is not defined in '$EXTENSION_NAME' extension")
    extension.repo ?: throw GradleException("Bintray repo is not defined in '$EXTENSION_NAME' extension")
    extension.groupId ?: throw GradleException("Bintray library groupId is not defined in '$EXTENSION_NAME' extension")
    extension.artifactId ?: throw GradleException("Bintray library artifactId is not defined in '$EXTENSION_NAME' extension")
    extension.version ?: throw GradleException("Bintray library version is not defined in '$EXTENSION_NAME' extension")
    extension.vcsUrl ?: throw GradleException("Bintray library vcsUrl is not defined in '$EXTENSION_NAME' extension")
    extension.license ?: throw GradleException("Bintray library license is not defined in '$EXTENSION_NAME' extension")
  }
}