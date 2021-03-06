package net.mox9.octokit

// TODO: Consider Product.showP
trait TabularKitPre {
  private def trimHeader(h: String): Int => String = {
    case i if i >= h.length => h
    case i if i > 5         => h.substring(0, i - 2) + ".."
    case i if i > 1         => h.substring(0, i - 1) + "-"
    case _                  => h.substring(0, 1)
  }

  @inline implicit final class ProductsWithTabular(private val xs: Trav[Product]) {
    @inline def tabularPs = {
      xs.headOption match {
        case None    => Nil
        case Some(h) =>
          val rows = xs.toVector map (_.productIterator.toVector map (_.toString))
          val cols = (0 until h.productArity).toVector map (idx => xs map (_.productElement(idx).toString))

          // TODO: deal with > 267 chars
          val widths = cols map (col => col map (_.length) max)

          val headers0 = h.getClass.getDeclaredFields.toVector map (_.getName)
          val headers = headers0 zip widths map Function.uncurried(trimHeader _).tupled

          val rowFormat = widths map ralign mkString " "
          (headers +: rows) map (row => rowFormat.format(row.seq: _*))
      }
    }
    @inline def showPs()  = tabularPs foreach println
  }

  @inline implicit final class MatrixWithTabular[T](private val xss: Trav[Trav[T]]) {
    @inline def tabularM = {
      val maxWidth = xss.toVector.foldLeft(0)((acc, x) => acc max x.size)

      val rows = xss.toVector map (_.toVector map (_.toString) padTo(maxWidth, ""))

      val cols = (0 until maxWidth).toVector map (idx => xss map (_.toIndexedSeq.applyOrElse(idx, (_: Int) => "").toString))

      val widths = cols map (col => col map (_.length) max)

      val rowFormat = widths map ralign mkString " "
      rows map (row => rowFormat.format(row.seq: _*))
    }
    @inline def showM()  = tabularM foreach println
  }

  @inline implicit final class MapWithTabular[K, V](private val xs: Trav[K -> V]) {
    @inline def maxKeyLen = xs.toIterator.map(_._1.toString.length).max
    @inline def tabularKV = xs map (kv => s"%${xs.maxKeyLen}s %s".format(kv._1, kv._2))
    @inline def showKV()  = tabularKV foreach println
  }

  @inline implicit final class MultimapWithTabular[K, V](private val xs: Trav[K -> Trav[V]]) {
    // TODO: alias xs.mkString("[", "],[", "]")
    @inline def tabularKVs = xs map (kv => s"%${xs.maxKeyLen}s %s".format(kv._1, kv._2.mkString("[", "],[", "]")))
    @inline def showKVs()  = tabularKVs foreach println
  }
}
