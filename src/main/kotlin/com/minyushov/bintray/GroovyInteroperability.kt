package com.minyushov.bintray

import groovy.lang.Closure
import org.gradle.internal.Cast.uncheckedCast


/**
 * Adapts a Kotlin function to a single argument Groovy [Closure].
 *
 * @param T the expected type of the single argument to the closure.
 * @param action the function to be adapted.
 *
 * @see [KotlinClosure1]
 */
internal fun <T : Any> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
  KotlinClosure1(action, this, this)


/**
 * Adapts a Kotlin function to a Groovy [Closure] that operates on the
 * configured Closure delegate.
 *
 * @param T the expected type of the delegate argument to the closure.
 * @param action the function to be adapted.
 *
 * @see [KotlinClosure1]
 */
internal fun <T> Any.delegateClosureOf(action: T.() -> Unit) =
  object : Closure<Unit>(this, this) {
    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall() = uncheckedCast<T>(delegate)?.action()
  }


/**
 * Adapts a parameterless Kotlin function to a parameterless Groovy [Closure].
 *
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
internal open class KotlinClosure0<V : Any>(
  val function: () -> V?,
  owner: Any? = null,
  thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {

  @Suppress("unused") // to be called dynamically by Groovy
  fun doCall(): V? = function()
}


/**
 * Adapts an unary Kotlin function to an unary Groovy [Closure].
 *
 * @param T the type of the single argument to the closure.
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
internal class KotlinClosure1<in T : Any, V : Any>(
  val function: T.() -> V?,
  owner: Any? = null,
  thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {

  @Suppress("unused") // to be called dynamically by Groovy
  fun doCall(it: T): V? = it.function()
}


/**
 * Adapts a binary Kotlin function to a binary Groovy [Closure].
 *
 * @param T the type of the first argument.
 * @param U the type of the second argument.
 * @param V the return type.
 * @param function the function to be adapted.
 * @param owner optional owner of the Closure.
 * @param thisObject optional _this Object_ of the Closure.
 *
 * @see [Closure]
 */
internal class KotlinClosure2<in T : Any, in U : Any, V : Any>(
  val function: (T, U) -> V?,
  owner: Any? = null,
  thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {

  @Suppress("unused") // to be called dynamically by Groovy
  fun doCall(t: T, u: U): V? = function(t, u)
}