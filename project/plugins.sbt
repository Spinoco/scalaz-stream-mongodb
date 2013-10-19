// The Typesafe repository
resolvers ++= Seq("Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

//to trace dependencies when the dependencies clashes
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")


addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.6.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")
