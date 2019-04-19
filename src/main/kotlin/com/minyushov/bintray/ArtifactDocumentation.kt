package com.minyushov.bintray

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import groovy.lang.Closure
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

internal abstract class ArtifactDocumentation : Artifact {
  override fun apply(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    if (extension.docs.getOrElse(true)) {
      configure(project, extension, publication)
    }
  }

  abstract fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication)
}

internal class ArtifactJavaDoc : ArtifactDocumentation() {
  override fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val javadocTask = project.tasks.named("javadoc", Javadoc::class.java) { task ->
      task.applyClosure(extension.docsSettings)
    }

    val javadocJarTask = project.tasks.register("javadocJar", Jar::class.java) { task ->
      task.archiveClassifier.set("javadoc")
      task.from(javadocTask.get().destinationDir)
    }

    javadocJarTask.dependsOn(javadocTask)

    publication.artifact(LazyPublishArtifact(javadocJarTask))
  }
}

internal class ArtifactAndroidDoc : ArtifactDocumentation() {
  override fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val android = project.extensions.getByType(LibraryExtension::class.java)
      ?: throw GradleException("Unable to find 'android' extension")

    val sourceSet = android.sourceSets.findByName("main")
      ?: throw GradleException("Unable to find 'main' source set")

    val javadocsTask = project.tasks.register("androidJavadocs", Javadoc::class.java) { task ->
      task.source(sourceSet.java.sourceFiles)
      task.classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
      task.applyClosure(extension.docsSettings)
    }

    val javadocJarTask = project.tasks.register("javadocJar", Jar::class.java) { task ->
      task.archiveClassifier.set("javadoc")
      task.from(javadocsTask.get().destinationDir)
    }

    javadocJarTask.dependsOn(javadocsTask)

    publication.artifact(LazyPublishArtifact(javadocJarTask))
  }
}

internal class ArtifactKotlinDoc : ArtifactDocumentation() {
  override fun configure(project: Project, extension: BintraySimpleExtension, publication: MavenPublication) {
    val dokkaTask = project.tasks.named("dokka", DokkaTask::class.java) { task ->
      task.outputFormat = "javadoc"
      task.applyClosure(extension.docsSettings)
    }

    val javadocJarTask = project.tasks.register("javadocJar", Jar::class.java) { task ->
      task.archiveClassifier.set("javadoc")
      task.from(dokkaTask.get().outputDirectory)
    }

    javadocJarTask.dependsOn(dokkaTask)

    publication.artifact(LazyPublishArtifact(javadocJarTask))
  }
}

private fun Task.applyClosure(closure: Closure<*>?) {
  if (closure != null) {
    closure.delegate = this
    closure.call()
  }
}