import com.banno._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

Docker.additionalRunCommands := Seq(Seq("uname", "-a"), Seq("whoami"))

Docker.entryPointArguments := Seq("testing", "arg1")
