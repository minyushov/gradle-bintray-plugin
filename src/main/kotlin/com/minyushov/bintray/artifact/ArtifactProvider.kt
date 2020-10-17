package com.minyushov.bintray.artifact

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.minyushov.bintray.BintrayExtension
import groovy.lang.Closure
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

internal class ArtifactProvider(
  private val project: Project,
  private val extension: BintrayExtension
) {

  val javaSources: PublishArtifact
    get() {
      val javaPlugin = project.convention.getPlugin<JavaPluginConvention>()
      val sourceSet = javaPlugin.sourceSets.getByName("main")

      val classesTask = project.tasks.named("classes")
      val sourcesJarTask = project.tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSet.allSource)
      }

      sourcesJarTask.dependsOn(classesTask)
      return LazyPublishArtifact(sourcesJarTask)
    }

  val androidSources: PublishArtifact
    get() {
      val android = project.extensions.getByType<LibraryExtension>()
      val sourceSet = android.sourceSets.findByName("main")
        ?: throw GradleException("Unable to find 'main' source set")

      val sourcesJarTask = project.tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSet.java.srcDirs)
      }

      return LazyPublishArtifact(sourcesJarTask)
    }

  val javadoc: PublishArtifact
    get() {
      val javadocTask = project.tasks.named<Javadoc>("javadoc") {
        applyClosure(extension.docsSettings)
      }

      val javadocJarTask = project.tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(javadocTask.map { it.destinationDir!! })
      }

      javadocJarTask.dependsOn(javadocTask)

      return LazyPublishArtifact(javadocJarTask)
    }

  val javadocAndroid: PublishArtifact
    get() {
      val android = project.extensions.getByType<LibraryExtension>()

      val sourceSet = android.sourceSets.findByName("main")
        ?: throw GradleException("Unable to find 'main' source set")

      val javadocsTask = project.tasks.register<Javadoc>("androidJavadocs") {
        source(sourceSet.java.getSourceFiles())

        classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))

        val variant = android.libraryVariants.find { it.name == "release" }
        if (variant != null) {
          classpath += variant.javaCompileProvider.get().classpath
        }

        applyClosure(extension.docsSettings)
      }

      val javadocJarTask = project.tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(javadocsTask.map { it.destinationDir!! })
      }

      javadocJarTask.dependsOn(javadocsTask)

      return LazyPublishArtifact(javadocJarTask)
    }

  val dokka: PublishArtifact
    get() {
      val dokkaTask = project.tasks.named<DokkaTask>("dokkaJavadoc") {
        applyClosure(extension.docsSettings)
      }

      val javadocJarTask = project.tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(dokkaTask.map { it.outputDirectory })
      }

      javadocJarTask.dependsOn(dokkaTask)

      return LazyPublishArtifact(javadocJarTask)
    }

  private fun Task.applyClosure(closure: Closure<*>?) {
    if (closure != null) {
      closure.delegate = this
      closure.call()
    }
  }

}