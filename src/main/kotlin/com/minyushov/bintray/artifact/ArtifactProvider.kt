package com.minyushov.bintray.artifact

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.minyushov.bintray.BintrayExtension
import groovy.lang.Closure
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

internal class ArtifactProvider(
  private val project: Project,
  private val extension: BintrayExtension
) {

  val javaSources: PublishArtifact
    get() {
      val javaPlugin = project.convention.getPlugin(JavaPluginConvention::class.java)
        ?: throw GradleException("Unable to find JavaPluginConvention")

      val sourceSet = javaPlugin.sourceSets.getByName("main")
        ?: throw GradleException("Unable to find main source set")

      val classesTask = project.tasks.named("classes")
      val sourcesJarTask = project.tasks.register("sourcesJar", Jar::class.java) { task ->
        task.archiveClassifier.set("sources")
        task.from(sourceSet.allSource)
      }

      sourcesJarTask.dependsOn(classesTask)
      return LazyPublishArtifact(sourcesJarTask)
    }

  val androidSources: PublishArtifact
    get() {
      val android = project.extensions.getByType(LibraryExtension::class.java)
        ?: throw GradleException("Unable to find 'android' extension")

      val sourceSet = android.sourceSets.findByName("main")
        ?: throw GradleException("Unable to find 'main' source set")

      val sourcesJarTask = project.tasks.register("sourcesJar", Jar::class.java) { task ->
        task.archiveClassifier.set("sources")
        task.from(sourceSet.java.srcDirs)
      }

      return LazyPublishArtifact(sourcesJarTask)
    }

  val javadoc: PublishArtifact
    get() {
      val javadocTask = project.tasks.named("javadoc", Javadoc::class.java) { task ->
        task.applyClosure(extension.docsSettings)
      }

      val javadocJarTask = project.tasks.register("javadocJar", Jar::class.java) { task ->
        task.archiveClassifier.set("javadoc")
        task.from(javadocTask.get().destinationDir)
      }

      javadocJarTask.dependsOn(javadocTask)

      return LazyPublishArtifact(javadocJarTask)
    }

  val javadocAndroid: PublishArtifact
    get() {
      val android = project.extensions.getByType(LibraryExtension::class.java)
        ?: throw GradleException("Unable to find 'android' extension")

      val sourceSet = android.sourceSets.findByName("main")
        ?: throw GradleException("Unable to find 'main' source set")

      val javadocsTask = project.tasks.register("androidJavadocs", Javadoc::class.java) { task ->
        task.source(sourceSet.java.sourceFiles)

        task.classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))

        val variant = android.libraryVariants.find { it.name == "release" }
        if (variant != null) {
          task.classpath += variant.javaCompileProvider.get().classpath
        }

        task.applyClosure(extension.docsSettings)
      }

      val javadocJarTask = project.tasks.register("javadocJar", Jar::class.java) { task ->
        task.archiveClassifier.set("javadoc")
        task.from(javadocsTask.get().destinationDir)
      }

      javadocJarTask.dependsOn(javadocsTask)

      return LazyPublishArtifact(javadocJarTask)
    }

  val dokka: PublishArtifact
    get() {
      val dokkaTask = project.tasks.named("dokka", DokkaTask::class.java) { task ->
        task.outputFormat = "javadoc"
        task.applyClosure(extension.docsSettings)
      }

      val javadocJarTask = project.tasks.register("javadocJar", Jar::class.java) { task ->
        task.archiveClassifier.set("javadoc")
        task.from(dokkaTask.get().outputDirectory)
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