import com.banno._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

Docker.additionalRunCommands := Seq(Seq("uname", "-a"), Seq("whoami"))

Docker.defaultCommand := "testing arg1"
