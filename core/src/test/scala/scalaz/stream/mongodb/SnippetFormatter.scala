package scalaz.stream.mongodb

import org.specs2.specification.{Fragments, Example}
import org.specs2.execute.Snippet
import Snippet._


trait SnippetFormatter {


  val extractIdx = """(\d+)\s+->\s+(.*)\s+}[.]verify""".r

  def formatSnippets[A](f: Fragments, examples:Map[Int, (String, A)]) : Fragments = {
    f.map {
      case ex: Example =>
        ex.copy(
          desc = ex.desc.map {
            s =>
              (for (extractIdx(index, content) <- extractIdx findAllIn s) yield (index, content)).toList.headOption match {
                case Some((idx, q)) =>
                  s"${examples(idx.toInt)._1.padTo(50, ' ')} : ${markdownCode(offset = 0)(q, q).padTo(100, ' ')} ${examples(idx.toInt)._2.toString}"
                case None =>
                  s
              }
          }
        )
      case other =>
        other
    }
  }




}
