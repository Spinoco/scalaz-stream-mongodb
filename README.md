Welcome to Mongo Streams
========================

[![Build Status](https://travis-ci.org/Spinoco/scalaz-stream-mongodb.png?branch=master)](https://travis-ci.org/Spinoco/scalaz-stream-mongodb)

Mongo Streams is a library that allows you to use scalaz-stream with mongo database. Mongo Streams are based on standard mongo java driver and support all of the java driver functionality,  plus the functionality offered by scalaz-stream. 

Please visit [project pages](http://spinoco.github.io/scalaz-stream-mongodb) for more information and [User Guide](http://spinoco.github.io/scalaz-stream-mongodb/reports/scalaz.stream.mongodb.userguide.UserGuideSpec.html) .
 
If you want to give try to mongo streams you can include it in your sbt build file : 

``` scala

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/releases")
 
libraryDependencies += "com.spinoco" %% "scalaz-stream-mongodb" % "0.1.0"

//if you want to add Specs2 support for your Mongo Streams just add also this
 
libraryDependencies += "com.spinoco" %% "scalaz-stream-mongodb-spec" % "0.1.0" 

 
```

The library should be compilable with scalaz.stream 0.4.0, we plan to upgrade it to 0.5.0 when that release will be available. 


