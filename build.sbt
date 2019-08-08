name := "scalaImageRecognition"

version := "0.1"

scalaVersion := "2.12.8"

val dl4jV = "1.0.0-beta4"

val akkaVersion="2.5.19"

val akkaHtpp="10.1.3"

val swaggerVersion = "2.0.8"

libraryDependencies ++= Seq(
  "org.nd4j" % "nd4j-native-platform" % dl4jV,
  "org.deeplearning4j" % "deeplearning4j-core" % dl4jV,
  "org.deeplearning4j" % "deeplearning4j-zoo" % dl4jV,
  "org.deeplearning4j" % "deeplearning4j-modelimport" % dl4jV,
  "org.datavec" % "datavec-data-image" % dl4jV,
  "org.openpnp" % "opencv" % "3.4.2-1",

  "org.postgresql" % "postgresql" % "42.1.1",
  "com.typesafe" % "config" % "1.3.2",
  "org.scalikejdbc" %% "scalikejdbc" % "2.5.2",
  "ch.qos.logback"  %  "logback-classic" % "1.2.3",

  "com.typesafe.akka" %% "akka-http" % akkaHtpp,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHtpp,

  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",

  "org.tensorflow" % "tensorflow" % "1.7.0"


)

javaOptions in run += "-Djava.library.path=C:\\Users\\dribagnac\\Downloads\\opencv\\build\\java\\x86"


libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

