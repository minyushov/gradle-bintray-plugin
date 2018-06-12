package com.minyushov.bintray

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class BintraySimpleExtension(project: Project) {
  val dryRun: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType).apply { set(true) }
  val user: Property<String> = project.objects.property(String::class.java)
  val key: Property<String> = project.objects.property(String::class.java)
  val groupId: Property<String> = project.objects.property(String::class.java)
  val artifactId: Property<String> = project.objects.property(String::class.java)
  val version: Property<String> = project.objects.property(String::class.java)
  val organization: Property<String> = project.objects.property(String::class.java)
  val repo: Property<String> = project.objects.property(String::class.java)
  val pkgName: Property<String> = project.objects.property(String::class.java)
  val vcsUrl: Property<String> = project.objects.property(String::class.java)
  val license: Property<String> = project.objects.property(String::class.java)
  val sources: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType).apply { set(true) }
  val docs: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType).apply { set(true) }
  var docsSettings: Closure<*>? = null
}