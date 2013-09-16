// The Typesafe repository
resolvers ++= Seq("Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

//to trace dependencies when the dependencies clashes
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
