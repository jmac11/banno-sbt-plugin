import com.banno._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

Docker.additionalRunCommands := Seq("uname -a ", "whoami"))

Docker.defaultCommand := "testing arg1"
