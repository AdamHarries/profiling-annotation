package lift
package profiler

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

class Profile extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro Profile.impl
}

object Profile {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val result = {
      annottees.map(_.tree).toList match {
        case q"$mods def $methodName[..$tpes](...$args): $returnType = { ..$body }" :: Nil => {
          q"""$mods def $methodName[..$tpes](...$args): $returnType =  {
            val start = System.nanoTime()
            val profSpliceResultValueNoConflict = {..$body}
            val end = System.nanoTime()
            println("PROFILING_DATUM: (\"${methodName.toString}\", " + (end-start) + ")")
            profSpliceResultValueNoConflict
          }"""
        }
        case _ => c.abort(c.enclosingPosition, "Annotation @Profile can be used only with methods")
      }
    }
    c.Expr[Any](result)
  }

  def profile[T](name: String,f: () => T) : T = {
    val start = System.nanoTime()
    val r: T = f()
    val end = System.nanoTime()
    println("PROFILING_DATUM: (\"${methodName.toString}\", " + (end-start) + ")")
    r
  }
}
