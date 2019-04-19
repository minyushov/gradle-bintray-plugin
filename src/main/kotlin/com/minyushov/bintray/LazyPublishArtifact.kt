package com.minyushov.bintray

import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.internal.artifacts.dsl.ArtifactFile
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import org.gradle.api.internal.provider.ProviderInternal
import org.gradle.api.internal.provider.Providers
import org.gradle.api.internal.tasks.AbstractTaskDependency
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.bundling.AbstractArchiveTask

import java.io.File
import java.util.Date

internal class LazyPublishArtifact : PublishArtifact {
  private val provider: Provider<*>
  private val version: String?
  private var delegate: PublishArtifact? = null

  constructor(provider: Provider<*>) {
    this.provider = provider
    this.version = null
  }

  constructor(provider: Provider<*>, version: String) {
    this.provider = provider
    this.version = version
  }

  override fun getName(): String =
    getDelegate().name

  override fun getExtension(): String =
    getDelegate().extension

  override fun getType(): String =
    getDelegate().type

  override fun getClassifier(): String? =
    getDelegate().classifier

  override fun getFile(): File =
    getDelegate().file

  override fun getDate(): Date? =
    Date()

  private fun getDelegate(): PublishArtifact {
    val delegate = this.delegate ?: when (val value = provider.get()) {
      is FileSystemLocation -> fromFile(value.asFile)
      is File -> fromFile(value)
      is AbstractArchiveTask -> ArchivePublishArtifact(value)
      else -> throw InvalidUserDataException("Cannot convert provided value ($value) to a file.")
    }
    this.delegate = delegate
    return delegate
  }

  private fun fromFile(file: File): DefaultPublishArtifact =
    ArtifactFile(file, version).let { artifact ->
      DefaultPublishArtifact(
        artifact.name,
        artifact.extension,
        artifact.extension,
        artifact.classifier,
        null,
        file
      )
    }

  override fun getBuildDependencies(): TaskDependency =
    object : AbstractTaskDependency() {
      override fun visitDependencies(context: TaskDependencyResolveContext) =
        context.add(provider)
    }
}
