package scalaz.stream.mongodb.bson

import org.specs2.{ScalaCheck, Specification}
import org.specs2.specification.Snippets
import com.mongodb.{BasicDBObjectBuilder, DBObject}
import scalaz.stream.mongodb.collectionSyntax
import collectionSyntax._
import java.util.Date
import scala.util.{Success, Try}
import org.scalacheck.{Gen, Arbitrary, Prop}
import org.bson.types.ObjectId

import scala.collection.JavaConverters._

import scala.language.reflectiveCalls


class BSONSpec extends Specification with ScalaCheck with Snippets {

  val dbObject: DBObject = BSONObject()
  val otherDbObject: DBObject = BSONObject()

  def is =
    s2"""

      ${"BSON Constructors"}


       Mongo Streams use standard BSON primitives from underlying Java Driver. However there is syntactic sugar that allows
       easy conversions of Scala datastructures (List, Maps, Sets and more) into DBObject. It is designed for maximum
       type-safety possible. That means You are not be able to construct DBObject that would contain invalid types (i.e. arbitrary class Foo)
       that would not be handled by Java BSON Serializer.


       To construct simple BSONObject just use:

       Simple               :  ${ snippet { BSONObject("int" -> int2BSONValue(1), "str" -> "str", "bool" -> true, "date" -> new Date(), "double" -> 0.1d) } }                          $simpleBson
       With lists, sets     :  ${ snippet { BSONObject("l1" -> BSONList(1, 2, 3), "s1" -> BSONSet(1, 2, 3, 4), "q1" -> BSONList(BSONSet("a", "b"), BSONList(1, 2))) }}                 $listBson
                            or ${ snippet { BSONObject("l1" -> List(1, 2, 3).asBSON, "s1" -> Set(1, 2, 3, 4).asBSON, "q1" -> BSONList(Set("a", "b").asBSON, List(1, 2).asBSON)) }}
       Nested objects       :  ${ snippet { BSONObject("o1" -> BSONObject("l2" -> BSONList(1, 2, 3)), "o2" -> BSONList(BSONObject("b" -> 1))) }}                                                     $nestedBson

       Special values:

       BSON Specification allows for null              :  ${ snippet { BSONObject("nullField" -> BSONNull) } }   $nullValue
       Options are added to object only when nonEmpty :  ${ snippet { BSONObject("one" -> Some(1)) } }                      $optionValue


       To get a value from DBObject You can use syntactic sugar

       To maybe get String value safely   :  ${ snippet { dbObject.tryGetAs[String]("s1"): Option[Try[String]] } }    ${retrieve.maybeSafe}
       To maybe get String value unsafe   :  ${ snippet { dbObject.getAs[String]("s1"): Option[String] } }            ${retrieve.maybeUnSafe}
       To get String value safely         :  ${ snippet { dbObject.tryAs[String]("s1"): Try[String] } }               ${retrieve.safe}
       To get String value safely         :  ${ snippet { dbObject.as[String]("s1"): String } }                       ${retrieve.unSafe}
                                          or ${ snippet { dbObject[String]("s1"): String } }

       You may also get collections from DBObject

       To get List[String]                :  ${ snippet { dbObject.getAs[List[String]]("s1"): Option[List[String]] } }                               ${retrieve.asList}
       To get Set[String]                 :  ${ snippet { dbObject.getAs[Set[String]]("s1"): Option[Set[String]] } }                                 ${retrieve.asSet}
       To get Map[String,String]          :  ${ snippet { dbObject.getAs[Map[String, String]]("s1"): Option[Map[String, String]] } }                 ${retrieve.asMap}
       To get Nested List                 :  ${ snippet { dbObject.getAs[List[Set[String]]]("s1"): Option[List[Set[String]]] } }                     ${retrieve.asNestedList}
       Or nested Set within Map           :  ${ snippet { dbObject.getAs[Map[String, Set[String]]]("s1"): Option[Map[String, Set[String]]] } }       ${retrieve.asNestedSetInMap}

       Finally, You may also modify content of the DBObject with simple operations:

       To add single key and value pair   :  ${ snippet { dbObject += ("s1" -> "string") } }                                ${modify.addOne}
       To add more key and value pairs    :  ${ snippet { dbObject ++= BSONObject("s1" -> "string", "s2" -> "string2") } }         ${modify.addMany}
                                          or ${ snippet { dbObject ++= otherDbObject }    }
       To remove single key               :  ${ snippet { dbObject -= "s1" } }                                              ${modify.removeOne}
       To remove more keys                :  ${ snippet { dbObject --= List("s1", "s2") } }                                 ${modify.removeMany}


    """

  //def is= s2"""$nestedBson"""

  def simpleBson = Prop.forAll {
    (i: Int, str: String, bool: Boolean, date: Long, double: Double) =>
      val build: DBObject = BSONObject("int" -> i, "str" -> str, "bool" -> bool, "date" -> new Date(date), "double" -> double)
      val dbo =
        BasicDBObjectBuilder.start
          .append("int", i)
          .append("str", str)
          .append("bool", bool)
          .append("date", new Date(date))
          .append("double", double)
          .get

      build must_== dbo
  }

  def listBson = Prop.forAll {
    (inl1: List[Int], ins1: Set[Int]) =>
      val build: DBObject = BSONObject("l1" -> inl1.asBSON, "s1" -> ins1.asBSON, "q1" -> BSONList(inl1.asBSON, ins1.asBSON))

      val dbo =
        BasicDBObjectBuilder.start
          .append("l1", inl1.asJava)
          .append("s1", ins1.asJava)
          .append("q1", List(inl1.asJava, ins1.asJava).asJava)
          .get

      (build.getAs[List[Int]]("l1") must_== dbo.getAs[List[Int]]("l1")) &&
        (build.getAs[Set[Int]]("l2") must_== dbo.getAs[Set[Int]]("l2")) &&
        (build.getAs[List[Iterable[Int]]]("q1") must_== dbo.getAs[List[Iterable[Int]]]("q1"))
  }

  def nestedBson = Prop.forAll {
    (l1: List[Int], i1: Int) =>
      val build: DBObject = BSONObject("o1" -> BSONObject("l2" -> l1.asBSON), "o2" -> BSONList(BSONObject("b" -> i1)))

      val dbo =
        BasicDBObjectBuilder.start
          .append("o1", BasicDBObjectBuilder.start("l2", l1.asJava).get)
          .append("o2", List(BasicDBObjectBuilder.start("b", i1).get).asJava)
          .get

      (build.as[Map[String,List[Int]]]("o1") must_== dbo.as[Map[String,List[Int]]]("o1")) &&
        (build.as[List[Map[String,Int]]]("o2") must_== dbo.as[List[Map[String,Int]]]("o2"))

  }

  def nullValue = Prop.forAll {
    (i: Int) =>
      val build: DBObject = BSONObject("i" -> i, "null" -> BSONNull)

      val dbo =
        BasicDBObjectBuilder.start
          .append("i", i)
          .append("null", null)
          .get

      build must_== dbo
  }

  def optionValue = Prop.forAll {
    (oi: Option[Int]) =>
      val build: DBObject = BSONObject("maybe" -> oi)

      val dbo =
        BasicDBObjectBuilder.start
          .get

      oi.foreach { i => dbo.put("maybe",i)}

      build must_== dbo
  }

  implicit lazy val oidGen = Arbitrary(Gen[ObjectId] { p => Some(new ObjectId(p.rng.nextInt(), p.rng.nextInt(), p.rng.nextInt())) })


  def retrieve = new {

    def buildObject(str: String, l: Long, d: Double, f: Float, bool: Boolean, oid: ObjectId) =
      BSONObject(
        "str" -> str,
        "byte" -> l.toByte,
        "short" -> l.toShort,
        "int" -> l.toInt,
        "long" -> l,
        "double" -> d,
        "float" -> f,
        "date" -> new Date(l),
        "bool" -> bool,
        "oid" -> oid
      )


    def maybeSafe = Prop.forAll {
      (str: String, l: Long, d: Double, f: Float, bool: Boolean, oid: ObjectId) =>

        val dbo: DBObject = buildObject(str, l, d, f, bool, oid)


        (dbo.tryGetAs[String]("str") must_== Some(Success(str))) &&
          (dbo.tryGetAs[Byte]("byte") must_== Some(Success(l.toByte))) &&
          (dbo.tryGetAs[Short]("short") must_== Some(Success(l.toShort))) &&
          (dbo.tryGetAs[Int]("int") must_== Some(Success(l.toInt))) &&
          (dbo.tryGetAs[Long]("long") must_== Some(Success(l))) &&
          (dbo.tryGetAs[Double]("double") must_== Some(Success(d))) &&
          (dbo.tryGetAs[Float]("float") must_== Some(Success(f))) &&
          (dbo.tryGetAs[Date]("date") must_== Some(Success(new Date(l)))) &&
          (dbo.tryGetAs[Boolean]("bool") must_== Some(Success(bool))) &&
          (dbo.tryGetAs[ObjectId]("oid") must_== Some(Success(oid))) &&
          (dbo.tryGetAs[String]("strNotExists") must_== None) &&
          (dbo.tryGetAs[Byte]("str").map(_.toOption) must_== Some(None))
    }


    def maybeUnSafe = Prop.forAll {
      (str: String, l: Long, d: Double, f: Float, bool: Boolean, oid: ObjectId) =>

        val dbo: DBObject = buildObject(str, l, d, f, bool, oid)


        (dbo.getAs[String]("str") must_== Some(str)) &&
          (dbo.getAs[Byte]("byte") must_== Some(l.toByte)) &&
          (dbo.getAs[Short]("short") must_== Some(l.toShort)) &&
          (dbo.getAs[Int]("int") must_== Some(l.toInt)) &&
          (dbo.getAs[Long]("long") must_== Some(l)) &&
          (dbo.getAs[Double]("double") must_== Some(d)) &&
          (dbo.getAs[Float]("float") must_== Some(f)) &&
          (dbo.getAs[Date]("date") must_== Some(new Date(l))) &&
          (dbo.getAs[Boolean]("bool") must_== Some(bool)) &&
          (dbo.getAs[ObjectId]("oid") must_== Some(oid)) &&
          (dbo.getAs[String]("strNotExists") must_== None)
    }


    def safe = Prop.forAll {
      (str: String, l: Long, d: Double, f: Float, bool: Boolean, oid: ObjectId) =>

        val dbo: DBObject = buildObject(str, l, d, f, bool, oid)


        (dbo.tryAs[String]("str") must_== Success(str)) &&
          (dbo.tryAs[Byte]("byte") must_== Success(l.toByte)) &&
          (dbo.tryAs[Short]("short") must_== Success(l.toShort)) &&
          (dbo.tryAs[Int]("int") must_== Success(l.toInt)) &&
          (dbo.tryAs[Long]("long") must_== Success(l)) &&
          (dbo.tryAs[Double]("double") must_== Success(d)) &&
          (dbo.tryAs[Float]("float") must_== Success(f)) &&
          (dbo.tryAs[Date]("date") must_== Success(new Date(l))) &&
          (dbo.tryAs[Boolean]("bool") must_== Success(bool)) &&
          (dbo.tryAs[ObjectId]("oid") must_== Success(oid)) &&
          (dbo.tryAs[Byte]("str").toOption must_== None)
    }

    def unSafe = Prop.forAll {
      (str: String, l: Long, d: Double, f: Float, bool: Boolean, oid: ObjectId) =>

        val dbo: DBObject = buildObject(str, l, d, f, bool, oid)


        (dbo.as[String]("str") must_== str) &&
          (dbo.as[Byte]("byte") must_== l.toByte) &&
          (dbo.as[Short]("short") must_== l.toShort) &&
          (dbo.as[Int]("int") must_== l.toInt) &&
          (dbo.as[Long]("long") must_== l) &&
          (dbo.as[Double]("double") must_== d) &&
          (dbo.as[Float]("float") must_== f) &&
          (dbo.as[Date]("date") must_== new Date(l)) &&
          (dbo.as[Boolean]("bool") must_== bool) &&
          (dbo.as[ObjectId]("oid") must_== oid)
    }


    def asList = Prop.forAll {
      (ls: List[String]) =>
        val dbo: DBObject = BSONObject("s1" -> ls.asBSON)

        dbo.as[List[String]]("s1") must_== ls
    }

    def asSet = Prop.forAll {
      (ss: Set[String]) =>
        val dbo: DBObject = BSONObject("s1" -> ss.asBSON)

        dbo.as[Set[String]]("s1") must_== ss
    }

    def asMap = Prop.forAll {
      (m: Map[String, String]) =>
        val dbo: DBObject = BSONObject("s1" -> m.asBSON)

        dbo.as[Map[String, String]]("s1") must_== m
    }

    def asNestedList = Prop.forAll {
      (ls: List[Set[String]]) =>
        val dbo: DBObject = BSONObject("s1" -> ls.asBSON)

        dbo.as[List[Set[String]]]("s1") must_== ls
    }

    def asNestedSetInMap = Prop.forAll {
      (m: Map[String, Set[String]]) =>
        val dbo: DBObject = BSONObject("s1" -> m.asBSON)

        dbo.as[Map[String, Set[String]]]("s1") must_== m
    }

  }


  def modify = new {

    def addOne = Prop.forAll {
      (i:Int) =>
        val dbo: DBObject = BSONObject()
        dbo += ("i"->i)

        val dbo2 = BasicDBObjectBuilder.start("i",i).get

        dbo must_== dbo2
    }

    def addMany = Prop.forAll {
      (i:Int,s:String) =>
        val dbo: DBObject = BSONObject()
        dbo ++= BSONObject("i"->i, "s"->s)

        val dbo2 = BasicDBObjectBuilder.start("i",i).append("s",s).get
        dbo must_== dbo2
    }

    def removeOne = Prop.forAll {
      (i:Int) =>
        val dbo: DBObject = BSONObject("i"->i)
        dbo -= "i"

        val dbo2 = BasicDBObjectBuilder.start.get

        dbo must_== dbo2
    }

    def removeMany = Prop.forAll {
      (i:Int) =>
        val dbo: DBObject = BSONObject("i"->i,"i2"->i)
        dbo --= List("i","i2")

        val dbo2 = BasicDBObjectBuilder.start.get

        dbo must_== dbo2
    }

  }

}
