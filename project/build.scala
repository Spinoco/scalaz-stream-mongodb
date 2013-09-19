


import sbt._
import sbt.Keys._
import com.typesafe.sbt._
import SbtGhPages._
import GhPagesKeys._
import SbtSite._
import SiteKeys._
import SbtGit._
import GitKeys._

object build extends Build {

  lazy val specs2Version = "2.3-scalaz-7.1.0-SNAPSHOT"

  lazy val resolverSettings =
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
      , Resolver.sonatypeRepo("snapshots")
      , "Spinoco Nexus Releases" at "https://maven.spinoco.com/nexus/content/repositories/releases/"
      , "Spinoco Nexus Snapshots" at "https://maven.spinoco.com/nexus/content/repositories/snapshots/"
    )


  lazy val credentialsSettings = Seq(
    credentials += {
      Seq("build.publish.user", "build.publish.password").map(k => Option(System.getProperty(k))) match {
        case Seq(Some(user), Some(pass)) =>
          Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
        case _ =>
          Credentials(Path.userHome / ".ivy2" / ".credentials")
      }
    }
  )

  lazy val libraries = Seq(
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.1.0-SNAPSHOT" withSources()
      , "org.scalaz" %% "scalaz-concurrent" % "7.1.0-SNAPSHOT" withSources()
      , "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.0-SNAPSHOT" % "test" withSources()
      , "org.mongodb" % "mongo-java-driver" % "2.11.2" withSources()
      , "spinoco" %% "scalaz-stream" % "0.1.0.26-SNAPSHOT" withSources()
    )
  )

  lazy val testLibraries =
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources()
      , "org.specs2" %% "specs2" % specs2Version % "test" withSources() exclude("org.scalaz", "*")
      , "org.pegdown" % "pegdown" % "1.2.1" % "test"
    )


  lazy val compileSettings = Seq(
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
    , scalacOptions in Test ++= Seq("-Yrangepos")
  )


  lazy val buildSettings =
    Defaults.defaultSettings ++
      Seq(
        organization := "spinoco"
        , version := "0.1.0-SNAPSHOT"
        , scalaVersion := "2.10.2"
        , conflictWarning ~= {
          cw =>
            cw.copy(filter = (id: ModuleID) => true, group = (id: ModuleID) => id.organization + ":" + id.name, level = Level.Error, failOnConflict = true)
        }
        , shellPrompt := ShellPrompt.buildShellPrompt
      ) ++
      resolverSettings ++
      credentialsSettings ++
      libraries ++
      testLibraries ++
      compileSettings ++
      net.virtualvoid.sbt.graph.Plugin.graphSettings


  /*lazy val siteSettings = ghpages.settings ++ SbtSite.site.settings ++ Seq(
    siteSourceDirectory <<= target(_ / "scalaz-stream-mongodb-reports"),
    // depending on the version, copy the api files to a different directory
    siteMappings <++= (mappings in packageDoc in Compile, version) map {
      (m, v) =>
        for ((f, d) <- m) yield (f, if (v.trim.endsWith("SNAPSHOT")) ("api/master/" + d) else ("api/streams-mongodb-" + v + "/" + d))
    },
    // override the synchLocal task to avoid removing the existing files
    synchLocal <<= (privateMappings, updatedRepository, gitRunner, streams) map {
      (mappings, repo, git, s) =>
        val betterMappings = mappings map { case (file, target) => (file, repo / target) }
        IO.copy(betterMappings)
        repo
    },
    gitRemoteRepo := "git@github.com:Spinoco/scalaz-stream-mongodb.git"
  )
        */

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //       PROJECTS

  lazy val main = Project("scalaz-stream-mongodb", file("."), settings = buildSettings)

  lazy val core = Project("scalaz-stream-mongodb-core", file("core"), settings = buildSettings).dependsOn(spec % "test")

  lazy val spec = Project("scalaz-stream-mongodb-spec", file("spec"), settings = buildSettings ++ Seq(
    libraryDependencies += "org.specs2" %% "specs2" % specs2Version withSources() exclude("org.scalaz", "*")
    , libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)
  ))

}

