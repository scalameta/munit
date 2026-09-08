import sbt.*
import sbt.Keys.*

object Extensions {
  def scala213 = "2.13.18"

  def scala212 = "2.12.21"

  def scala3 = "3.3.8"

  def scala3next = "3.8.4"

  implicit class ProjectConfigureExtensions(private val f: Project => Project)
      extends AnyVal {
    def settings(ss: Def.SettingsDefinition*): Project => Project = f
      .andThen(_.settings(ss *))
  }

  /* `bspEnabled := false` leaves a row out of the BSP workspace, so an IDE
   * does not import it. IntelliJ can't load multiple versions, though, so
   * force 2.13 if `ide.scala` is absent, and the JVM alone if `ide.platform`
   * is. */
  private val isIntelliJ = sys.props.contains("idea.managed")

  private val ideScala = {
    val prop = sys.props.getOrElse("ide.scala", "").trim
    if (prop.nonEmpty) Some(prop) else if (isIntelliJ) Some(scala213) else None
  }

  /* Scala.js and Native hold little code here, and compiling them costs a BSP
   * client its first build, so this build offers the JVM alone until someone
   * asks for more. */
  private val defaultPlatforms = Set(VirtualAxis.jvm.value)

  // an empty set is no filter, so every platform
  private val idePlatforms = sys.props.get("ide.platform")
    .fold(defaultPlatforms)(_.split(',').map(_.trim).filter(_.nonEmpty).toSet)

  // only ever disables a row, so it never overrides another setting
  private def ideSkip(
      platform: VirtualAxis.PlatformAxis,
      version: String,
  ): Seq[Setting[?]] = {
    val skip = idePlatforms.nonEmpty && !idePlatforms(platform.value) ||
      version.nonEmpty && ideScala.exists(s =>
        s != version && s != CrossVersion.binaryScalaVersion(version)
      )
    if (skip) Seq(bspEnabled := false) else Nil
  }

  implicit class MatrixExtensions(private val self: ProjectMatrix)
      extends AnyVal {
    // one row at a time, so each one knows the version it is built for
    private def cross(platform: VirtualAxis.PlatformAxis)(
        add: (
            ProjectMatrix,
            String,
            Seq[VirtualAxis],
            Project => Project,
        ) => ProjectMatrix
    )(versions: Seq[String], axes: Seq[VirtualAxis], ss: Seq[Setting[?]])(
        f: Project => Project
    ): ProjectMatrix = versions.foldLeft(self)((m, v) =>
      add(m, v, axes, f.andThen(_.settings(ss ++ ideSkip(platform, v) *)))
    )

    def crossJvm(
        versions: Seq[String],
        axes: Seq[VirtualAxis] = Nil,
        ss: Seq[Setting[?]] = Nil,
    )(f: Project => Project = identity): ProjectMatrix = cross(VirtualAxis.jvm)(
      (m, v, a, g) => m.jvmPlatform(Seq(v), a, g)
    )(versions, axes, ss)(f)

    def crossJs(
        versions: Seq[String],
        axes: Seq[VirtualAxis] = Nil,
        ss: Seq[Setting[?]] = Nil,
    )(f: Project => Project = identity): ProjectMatrix = cross(VirtualAxis.js)(
      (m, v, a, g) => m.jsPlatform(Seq(v), a, g)
    )(versions, axes, ss)(f)

    def crossNative(
        versions: Seq[String],
        axes: Seq[VirtualAxis] = Nil,
        ss: Seq[Setting[?]] = Nil,
    )(f: Project => Project = identity): ProjectMatrix = cross(
      VirtualAxis.native
    )((m, v, a, g) => m.nativePlatform(Seq(v), a, g))(versions, axes, ss)(f)
  }
}
