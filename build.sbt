import pre.SbtKit._

organization := "net.mox9.skithub"
        name := "skithub"
     version := "1.0-SNAPSHOT"

      scalaVersion := "2.11.6"
crossScalaVersions := Seq(scalaVersion.value)

scalacOptions            ++= Seq("-encoding", "utf8")
scalacOptions            ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
scalacOptions             += "-language:postfixOps"
scalacOptions             += "-Xfatal-warnings"
scalacOptions             += "-Xfuture"
scalacOptions             += "-Yinline-warnings"
scalacOptions             += "-Yno-adapted-args"
scalacOptions             += "-Ywarn-dead-code"
scalacOptions             += "-Ywarn-numeric-widen"
scalacOptions in compile ++= "-Ywarn-unused-import".ifScala211Plus.value.toSeq
scalacOptions             += "-Ywarn-value-discard"

maxErrors := 5
triggeredMessage := Watched.clearWhenTriggered

  wartremoverWarnings += Wart.Any
  wartremoverWarnings += Wart.Any2StringAdd
  wartremoverWarnings += Wart.AsInstanceOf
  wartremoverWarnings += Wart.EitherProjectionPartial
  wartremoverWarnings += Wart.IsInstanceOf
  wartremoverWarnings += Wart.ListOps
  wartremoverWarnings += Wart.JavaConversions
  wartremoverWarnings += Wart.MutableDataStructures
//wartremoverWarnings += Wart.NonUnitStatements       // bans this.type :(
  wartremoverWarnings += Wart.Null
  wartremoverWarnings += Wart.OptionPartial
  wartremoverWarnings += Wart.Return
  wartremoverWarnings += Wart.TryPartial
  wartremoverWarnings += Wart.Var

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.3.9"

initialCommands in console := "import net.mox9.skithub._"

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
