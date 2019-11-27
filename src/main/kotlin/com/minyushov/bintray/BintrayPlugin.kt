package com.minyushov.bintray

import com.minyushov.bintray.project.createBintrayProject
import org.gradle.api.Plugin
import org.gradle.api.Project

class BintrayPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project
      .createBintrayProject(
        project
          .extensions
          .create(EXTENSION_NAME, BintrayExtension::class.java, project)
      )
      ?.configure()
      ?: return
  }

  companion object {
    const val EXTENSION_NAME = "bintray"
  }
}