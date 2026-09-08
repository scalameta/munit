import sbt.*

object Extensions {
  implicit class ProjectConfigureExtensions(private val f: Project => Project)
      extends AnyVal {
    def settings(ss: Def.SettingsDefinition*): Project => Project = f
      .andThen(_.settings(ss *))
  }

  implicit class MatrixExtensions(private val self: ProjectMatrix)
      extends AnyVal {
    // one row at a time, so each one knows the version it is built for
    private def cross(
        add: (
            ProjectMatrix,
            String,
            Seq[VirtualAxis],
            Project => Project,
        ) => ProjectMatrix
    )(versions: Seq[String], axes: Seq[VirtualAxis], ss: Seq[Setting[?]])(
        f: Project => Project
    ): ProjectMatrix = versions
      .foldLeft(self)((m, v) => add(m, v, axes, f.andThen(_.settings(ss *))))

    def crossJvm(
        versions: Seq[String],
        axes: Seq[VirtualAxis] = Nil,
        ss: Seq[Setting[?]] = Nil,
    )(f: Project => Project = identity): ProjectMatrix =
      cross((m, v, a, g) => m.jvmPlatform(Seq(v), a, g))(versions, axes, ss)(f)

    def crossJs(
        versions: Seq[String],
        axes: Seq[VirtualAxis] = Nil,
        ss: Seq[Setting[?]] = Nil,
    )(f: Project => Project = identity): ProjectMatrix =
      cross((m, v, a, g) => m.jsPlatform(Seq(v), a, g))(versions, axes, ss)(f)

    def crossNative(
        versions: Seq[String],
        axes: Seq[VirtualAxis] = Nil,
        ss: Seq[Setting[?]] = Nil,
    )(f: Project => Project = identity): ProjectMatrix = cross((m, v, a, g) =>
      m.nativePlatform(Seq(v), a, g)
    )(versions, axes, ss)(f)
  }
}
