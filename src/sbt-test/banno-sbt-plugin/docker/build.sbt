import com.banno._
import Docker._

name := "sbt-docker-test"

BannoSettings.settings

Docker.settings

appDir in docker := file("/test")

additionalRunShellCommands in docker := Seq("uname -a", "whoami")

additionalRunExecCommands in docker += Seq("echo", "hello")

entryPointPrelude in docker := "TEST_VAR=`echo -n test_env_var`"

command in docker := Seq("testing", "arg1")
