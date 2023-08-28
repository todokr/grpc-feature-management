import scalapb.compiler.Version.{grpcJavaVersion, scalapbVersion}

ThisBuild / scalaVersion := "2.13.8"

val ProtobufSourceDir = file("../../../api_schema")
Compile / PB.protoSources := Seq(ProtobufSourceDir)
Compile / PB.targets := Seq(
  scalapb.gen(
    grpc = true, // protobufに定義されたServiceに対応するコードを生成する
    flatPackage = true, // protobufのファイル名をpackage名に含めない
    lenses = false // Lens（雑に言うと関数型スタイルのgetter, setterみたいなやつ）は必要ないのでオフ
  ) -> (Compile / sourceManaged).value
)

lazy val root = (project in file("."))
  .settings(
    name := "Feature Management Api (ScalaPB Minimum)",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion,
      "org.wvlet.airframe" %% "airframe-log" % "22.8.0"
    )
  )
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
