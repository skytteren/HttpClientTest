name := "httpClientTest"

scalaVersion := "2.10.2"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.0"

libraryDependencies += "com.ning" % "async-http-client" % "1.7.19"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0.M6-SNAP34" % "test"

libraryDependencies += "org.apache.httpcomponents" % "httpasyncclient" % "4.0-beta4"

resolvers += "spray repo" at "http://nightlies.spray.io"

libraryDependencies += "io.spray" % "spray-client" % "1.2-20130710"

fork in run := true

libraryDependencies += "org.eclipse.jetty" % "jetty-client" % "9.0.4.v20130625"

