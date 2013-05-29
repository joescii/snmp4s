import sbt._
object Snmp4sSbtPlugin extends Plugin
{
    // configuration points, like the built in `version`, `libraryDependencies`, or `compile`
    // by implementing Plugin, these are automatically imported in a user's `build.sbt`
    val genMibsTask = TaskKey[Unit]("snmp4s-gen-mibs")
    val builtInMibsSetting = SettingKey[String]("snmp4s-built-in-mibs")

    // a group of settings ready to be added to a Project
    // to automatically add them, do
    val snmp4sSettings = Seq(
        builtInMibsSetting := "IfMib",
        genMibsTask <<= builtInMibsSetting map { mib =>  }
    )

    // alternatively, by overriding `settings`, they could be automatically added to a Project
    // override val settings = Seq(...)
}
