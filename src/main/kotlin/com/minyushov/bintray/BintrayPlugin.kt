package com.minyushov.bintray

import com.minyushov.bintray.project.createBintrayProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class BintrayPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project
      .createBintrayProject(
        project
          .extensions
          .create(EXTENSION_NAME, BintrayExtension::class, project)
      )
      ?.configure()
      ?: return
  }

  companion object {
    const val EXTENSION_NAME = "bintray"
  }
}