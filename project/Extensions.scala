import sbt.{Def, Project}

object Extensions {
  implicit class ProjectConfigureExtensions(private val f: Project => Project)
      extends AnyVal {
    def settings(ss: Def.SettingsDefinition*): Project => Project = f
      .andThen(_.settings(ss: _*))
  }
}
