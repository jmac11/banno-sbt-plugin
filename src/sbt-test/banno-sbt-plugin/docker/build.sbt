import com.banno._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

Docker.additionalRunCommands := Seq("whoami | grep -q root")

Docker.entryPointArguments := Seq("testing", "arg1")
