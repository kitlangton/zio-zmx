import sbt._
import sbt.Keys._
import sbtbuildinfo._
import BuildInfoKeys._

object BuildHelper {
  private val Scala212 = "2.12.12"
  private val Scala213 = "2.13.4"

  private val stdOptions = Seq(
    "-encoding",
    "UTF-8",
    "-explaintypes",
    "-Yrangepos",
    "-feature",
    "-language:higherKinds",
    "-language:existentials",
    "-Xlint:_,-type-parameter-shadow",
    "-Xsource:2.13",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-unchecked",
    "-deprecation",
    "-Xfatal-warnings"
  )

  private val stdOpts213 = Seq(
    "-Wunused:imports",
    "-Wvalue-discard",
    "-Wunused:patvars",
    "-Wunused:privates",
    "-Wunused:params",
    "-Wvalue-discard"
  )

  private val stdOptsUpto212 = Seq(
    "-Xfuture",
    "-Ypartial-unification",
    "-Ywarn-nullary-override",
    "-Yno-adapted-args",
    "-Ywarn-infer-any",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-unit",
    "-Ywarn-unused-import"
  )

  private def silencerVersion(scalaVersion: String) = scalaVersion match {
    case "2.13.1" => "1.6.0"
    case "2.13.2" => "1.6.0"
    case "2.13.4" => "1.7.3"
    case _        => "1.7.1"
  }

  private def extraOptions(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) =>
        stdOpts213
      case Some((2, 12)) =>
        Seq(
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:_,imports",
          "-Ywarn-unused:imports",
          "-opt:l:inline",
          "-opt-inline-from:<source>"
        ) ++ stdOptsUpto212
      case _             =>
        Seq("-Xexperimental") ++ stdOptsUpto212
    }

  def buildInfoSettings(packageName: String) =
    Seq(
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
      buildInfoPackage := packageName,
      buildInfoObject := "BuildInfo"
    )

  def stdSettings(prjName: String) =
    Seq(
      name := s"$prjName",
      crossScalaVersions := Seq(Scala212, Scala213),
      ThisBuild / scalaVersion := Scala213,
      scalacOptions := stdOptions ++ extraOptions(scalaVersion.value),
      libraryDependencies ++=
        Seq(
          "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.4",
          ("com.github.ghik"        % "silencer-lib"            % silencerVersion(scalaVersion.value) % Provided)
            .cross(CrossVersion.full),
          compilerPlugin(
            ("com.github.ghik"      % "silencer-plugin"         % silencerVersion(scalaVersion.value)).cross(CrossVersion.full)
          ),
          compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
        ),
      incOptions ~= (_.withLogRecompileOnMacro(false)),
      Test / parallelExecution := false
    )
}
