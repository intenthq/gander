import sbt._

organization := "com.intenthq"

organizationName := "Intent HQ"

organizationHomepage := Some(url("http://www.intenthq.com"))

name := "gander"

description := "Extracts text, metadata from web pages."

homepage := Some(url("https://github.com/intenthq/gander"))

developers := List(
  Developer(id = "albertpastrana", name = "Albert Pastrana", email = "", url = new URL("https://github.com/albertpastrana")),
  Developer(id = "ArturSoler", name = "Artur Soler", email = "", url = new URL("https://github.com/ArturSoler"))
)

scmInfo := Some(
  ScmInfo(
    browseUrl = new URL("https://github.com/intenthq/gander"),
    connection = "scm:git:git@github.com:intenthq/gander.git"
  )
)

licenses += "Apache2" -> url("http://www.apache.org/licenses/")

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.4")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

Defaults.itSettings

scalacOptions ++= Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-unchecked",
  "-deprecation",
  "-feature")

testOptions in Test += Tests.Argument("-oF")

credentials += Credentials(Path.userHome / ".ivy2" / ".maven-credentials")

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "19.0",
  "joda-time" % "joda-time" % "2.9.3",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.jsoup" % "jsoup" % "1.9.1",
  "org.slf4j"	% "slf4j-api"	% "1.7.21",
  "org.specs2" %% "specs2-core" % "4.0.2" % "it,test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation")

publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")

releasePublishArtifactsAction := PgpKeys.publishSigned.value

lazy val root = project.in(file(".")).configs(IntegrationTest)
