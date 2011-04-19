object Nexus {
  import org.apache.ivy.util.url.CredentialsStore
  import dispatch._
  import Http._
  
  def runScheduledJob(jobId: String)(f: (String, String) => Unit) {
    val creds = CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager", "10.3.0.26")
    val request = :/("10.3.0.26", 8081) / "nexus/service/local/schedule_run/" / jobId as (creds.getUserName, creds.getPasswd)
    Http(request <> { xml =>
      val status = (xml \\ "status")(0).text
      val created = (xml \\ "created")(0).text
      f(status,created)               
   })
  }
}
