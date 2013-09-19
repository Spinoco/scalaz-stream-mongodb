Welcome to Mongo Streams
=====================

Mongo Streams is a library that allows you to use scalaz-stream with mongo database. Mongo Streams is based on standard mongo java driver and thus shall support all of the java driver functionality.

Project is now under development and first release is expected after first release of scalaz-stream.

Building the library from sources

At this moment (unfortunately) there is no binary version of the library. In order to build it, you will have to get latest snapshots of scalaz, scalaz-stream, specs2 (please use the version build against the 7.1.0-SNAPSHOT). Once the library will be released all these dependencies will get fixed.

## User Guide

If you are interested on the functionality please take a look on [UserGuide](http://spinoco.github.io/scalaz-stream-mongodb/reports/scalaz.stream.mongodb.userguide.UserGuideSpec.html) and eventually on the [API](http://spinoco.github.io/scalaz-stream-mongodb/core/#package).

## What is missing

Library is supposed to have all the functionality that makes sense to wrap in scalaz-stream. Following functionality is currently missing and is planned for future versions:

* GridFS
* Aggregation framework
* Javascript queries
* Asynchronous execution of mongodb queries


Thank you for your interest in Mongo Streams.
