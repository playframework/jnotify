import bintray.Keys._

organization := "net.contentobjects.jnotify"
name := "jnotify"
version := s"${jnotifyVersion.value}-play-1"
description := "jnotify"
homepage := Some(url("https://github.com/playframework/jnotify"))

crossPaths := false
autoScalaLibrary := false

lazy val jnotifyVersion = settingKey[String]("The version of JNotify")
jnotifyVersion := "0.94"

lazy val downloadAndExtractJnotify = taskKey[File]("Download and extract JNotify")
downloadAndExtractJnotify := {
  val v = jnotifyVersion.value
  val zip = target.value / s"jnotify-$v.zip"
  IO.download(new URL(s"http://downloads.sourceforge.net/project/jnotify/jnotify/jnotify-$v/jnotify-lib-$v.zip"), zip)
  val jnotify = target.value / "jnotify"
  jnotify.mkdirs()

  IO.unzip(zip, jnotify)

  jnotify
}

// We actually use a hacked jnotify jar, don't know where it comes from, but it packages all the binaries in it.
mappings in (Compile, packageBin) := {
  val jnotify = downloadAndExtractJnotify.value
  val jnotifyJarExtracted = target.value / "jnotify-jar"
  IO.unzip(jnotify / s"jnotify-${jnotifyVersion.value}.jar", jnotifyJarExtracted)

  val jnotifyMappings = jnotifyJarExtracted.***.filter(f => f.isFile && f.getName != "MANIFEST.MF") pair relativeTo(jnotifyJarExtracted)

  def lib(bits: Int, name: String) = s"native_libraries/${bits}bits/$name"

  // Due to https://github.com/sbt/sbt/issues/1972, we need to copy libjnotify.jnilib somewhere else in order to
  // package it twice
  val jnilib = jnotify / "libjnotify.jnilib"
  val jnilib2 = target.value / "libjnotify.jnilib2"
  IO.copyFile(jnilib, jnilib2)

  val nativeLibs = Seq(
    (jnotify / "jnotify.dll") -> lib(32, "jnotify.dll"),
    (jnotify / "jnotify_64bit.dll") -> lib(64, "jnotify_64bit.dll"),

    (jnotify / "libjnotify.so") -> lib(32, "libjnotify.so"),
    (jnotify / "64-bit Linux" / "libjnotify.so") -> lib(64, "libjnotify.so"),

    // I don't actually know what .jnilib is, but yes, the same one is used on 32 and 64 bit platforms
    jnilib -> lib(32, "libjnotify.jnilib"),
    jnilib2 -> lib(64, "libjnotify.jnilib")
  )

  jnotifyMappings ++ nativeLibs
}
packageOptions in (Compile, packageBin) += {
  import java.util.jar.Attributes.Name
  Package.ManifestAttributes(
    // So that anyone wondering where the heck this jar came from (as I was with the one we used to use on Play)
    // can find out
    new Name("Repackage-Project") -> "https://github.com/playframework/jnotify"
  )
}


packageSrc in Compile := downloadAndExtractJnotify.value / s"jnotify-${jnotifyVersion.value}-src.zip"

publishArtifact in Test := false
publishArtifact in packageDoc := false

bintraySettings
licenses += ("LGPL-2.1", url("https://www.gnu.org/licenses/lgpl-2.1.html"))
(vcsUrl in bintray) := Some("pserver:anonymous@jnotify.cvs.sourceforge.net:/cvsroot/jnotify")
publishMavenStyle := false
repository in bintray := "sbt-plugin-releases"
bintrayOrganization in bintray := Some("playframework")