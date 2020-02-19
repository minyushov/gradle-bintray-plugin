package com.minyushov.bintray

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class BintrayExtension(project: Project) {
  val dryRun: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType).apply { set(true) }
  val user: Property<String> = project.objects.property(String::class)
  val key: Property<String> = project.objects.property(String::class)
  val groupId: Property<String> = project.objects.property(String::class)
  val artifactId: Property<String> = project.objects.property(String::class)
  val version: Property<String> = project.objects.property(String::class)
  val organization: Property<String> = project.objects.property(String::class)
  val repo: Property<String> = project.objects.property(String::class)
  val pkgName: Property<String> = project.objects.property(String::class)
  val vcsUrl: Property<String> = project.objects.property(String::class)
  val license: Property<String> = project.objects.property(String::class)
  val sources: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType).apply { set(true) }
  val docs: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType).apply { set(true) }
  val variant: Property<String> = project.objects.property(String::class).apply { set("release") }
  var docsSettings: Closure<*>? = null
}