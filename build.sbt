val scalaV = "2.12.1"

lazy val http4sVersion = "0.15.0a"

version in ThisBuild := "0.1.0"

maintainer := "Mark Eibes <mark.eibes@gmail.com>"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "jitpack" at "https://jitpack.io"
)

lazy val doobieVersion = "0.3.1-M3"

lazy val katexVersion = "0.6.0"

lazy val server = (project in file("server"))
  .settings(
    scalaVersion := scalaV,
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
//    compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline.map(
      f => f(Seq.empty))).value,
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "javax.mail"        % "mail"                 % "1.4.7",
      "ch.qos.logback"    % "logback-classic"      % "1.1.7",
      "com.lihaoyi"       %% "scalatags"           % "0.6.2",
      "com.roundeights"   %% "hasher"              % "1.2.0",
      "org.http4s"        %% "http4s-dsl"          % http4sVersion,
      "org.http4s"        %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"        %% "http4s-blaze-client" % http4sVersion,
      "org.tpolecat"      %% "doobie-core"         % doobieVersion,
      "org.tpolecat"      %% "doobie-postgres"     % doobieVersion,
      "com.vmunier"       %% "scalajs-scripts"     % "1.1.0"
    ),
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .enablePlugins(SbtWeb, JavaAppPackaging)
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .settings(
    scalaVersion := scalaV,
    persistLauncher := true,
    persistLauncher in Test := false,
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "org.scala-js"                      %%% "scalajs-dom"  % "0.9.1",
      "io.monix"                          %%% "monix"        % "2.1.1",
      "com.github.japgolly.scalajs-react" %%% "core"         % "0.11.3",
      "com.github.japgolly.scalajs-react" %%% "ext-scalaz72" % "0.11.3",
      "com.github.japgolly.scalajs-react" %%% "ext-monocle"  % "0.11.3",
      "com.github.japgolly.scalajs-react" %%% "extra"        % "0.11.3",
      "com.github.mpilquist"              %%% "simulacrum"   % "0.10.0"
    ),
    jsDependencies ++= Seq(
//      "org.webjars.npm" % "katex" % katexVersion
//                             / "katex.js"
//                             minified "katex.min.js"
//                             commonJSName "katex",
                           "org.webjars.bower" % "react" % "15.3.2"
                             / "react-with-addons.js"
                             minified "react-with-addons.min.js"
                             commonJSName "React",
                           "org.webjars.bower" % "react" % "15.3.2"
                             / "react-dom.js"
                             minified "react-dom.min.js"
                             dependsOn "react-with-addons.js"
                             commonJSName "ReactDOM",
                           "org.webjars.bower" % "react" % "15.3.2"
                             / "react-dom-server.js"
                             minified "react-dom-server.min.js"
                             dependsOn "react-dom.js"
                             commonJSName "ReactDOMServer")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)

lazy val scodecCoreVersion = "1.10.3"
lazy val scodecBitsVersion = "1.1.2"
lazy val scalazVersion     = "7.2.8"
lazy val monocleVersion    = "1.3.2"

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := scalaV,
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scodec"                 %%% "scodec-core"     % scodecCoreVersion,
      "org.scodec"                 %%% "scodec-bits"     % scodecBitsVersion,
      "com.github.julien-truffaut" %%% "monocle-core"    % monocleVersion,
      "com.github.julien-truffaut" %%% "monocle-generic" % monocleVersion,
      "com.github.julien-truffaut" %%% "monocle-macro"   % monocleVersion,
      "org.scalaz"                 %%% "scalaz-core"     % scalazVersion,
      "org.scalaz"                 %%% "scalaz-effect"   % scalazVersion
    ))
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scodec"                 %% "scodec-core"     % scodecCoreVersion,
      "org.scodec"                 %% "scodec-bits"     % scodecBitsVersion,
      "com.github.julien-truffaut" %% "monocle-core"    % monocleVersion,
      "com.github.julien-truffaut" %% "monocle-generic" % monocleVersion,
      "com.github.julien-truffaut" %% "monocle-macro"   % monocleVersion,
      "org.scalaz"                 %% "scalaz-core"     % scalazVersion,
      "org.scalaz"                 %%% "scalaz-effect"  % scalazVersion
    )
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm

lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command
  .process("project server", _: State)) compose (onLoad in Global).value

cancelable in Global := true

fork := true
