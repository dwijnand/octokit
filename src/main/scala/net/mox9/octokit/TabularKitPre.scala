package net.mox9.octokit

import scala.language.higherKinds
import scala.language.implicitConversions

trait TabularKitPre {
  private def trimHeader(h: String): Int => String = {
    case i if i >= h.length => h
    case i if i > 5         => h.substring(0, i - 2) + ".."
    case i if i > 1         => h.substring(0, i - 1) + "-"
    case _                  => h.substring(0, 1)
  }

  @inline implicit final class ProductsWithTabular(private val xs: Trav[Product]) {
    @inline def tabularps = {
      xs.headOption match {
        case None    => Nil
        case Some(h) =>
          val rows = xs.toVector map (_.productIterator.toVector map (_.toString))
          val cols = (0 until h.productArity).toVector map (idx => xs map (_.productElement(idx).toString))

          val widths = cols map (col => col map (_.length) max)

          val headers0 = h.getClass.getDeclaredFields.toVector map (_.getName)
          val headers = headers0 zip widths map Function.uncurried(trimHeader _).tupled

          val rowFormat = widths map ralign mkString " "
          (headers +: rows) map (row => rowFormat.format(row.seq: _*))
      }
    }
    @inline def showps()  = tabularps foreach println
  }

  @inline implicit final class MapWithTabular[K, V](private val xs: Trav[K -> V]) {
    @inline def maxKeyLen = xs.toIterator.map(_._1.toString.length).max
    @inline def tabularKV = xs map (kv => s"%${xs.maxKeyLen}s %s".format(kv._1, kv._2))
    @inline def showKV()  = tabularKV foreach println
  }

  @inline implicit final class MultimapWithTabular[K, V](private val xs: Trav[K -> Trav[V]]) {
    @inline def tabularKVs = xs map (kv => s"%${xs.maxKeyLen}s %s".format(kv._1, kv._2.mkString("[", "],[", "]")))
    @inline def showKVs()  = tabularKVs foreach println
  }
}
