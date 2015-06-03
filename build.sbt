import SbtKitPre._

organization := "net.mox9.octokit"
        name := "octokit"
     version := "1.0-SNAPSHOT"

      scalaVersion := "2.11.6"
crossScalaVersions := Seq(scalaVersion.value)

scalacOptions            ++= Seq("-encoding", "utf8")
scalacOptions            ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
scalacOptions             += "-language:higherKinds"
scalacOptions             += "-language:implicitConversions"
scalacOptions             += "-language:postfixOps"
scalacOptions in compile  += "-Xfatal-warnings"
scalacOptions             += "-Xfuture"
scalacOptions             += "-Yinline-warnings"
scalacOptions             += "-Yno-adapted-args"
scalacOptions             += "-Ywarn-dead-code"
scalacOptions             += "-Ywarn-numeric-widen"
scalacOptions in compile ++= "-Ywarn-unused-import".ifScala211Plus.value.toSeq
scalacOptions             += "-Ywarn-value-discard"

maxErrors := 5
triggeredMessage := Watched.clearWhenTriggered

//wartremoverWarnings += Wart.Any                     // bans f-interpolator
  wartremoverWarnings += Wart.Any2StringAdd
  wartremoverWarnings += Wart.AsInstanceOf
  wartremoverWarnings += Wart.EitherProjectionPartial
//wartremoverWarnings += Wart.FinalCaseClass          // False positive on AnyVal: #174
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

libraryDependencies += "com.typesafe.play" %% "play-cache" % "2.4.0"
libraryDependencies += "com.typesafe.play" %% "play-ws"    % "2.4.0"

initialCommands in console += "\nimport net.mox9.octokit._"
initialCommands in console += "\nval m = new Main ; import m._ ; import actorSystem.dispatcher"

fork in run := true
cancelable in Global := true

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
