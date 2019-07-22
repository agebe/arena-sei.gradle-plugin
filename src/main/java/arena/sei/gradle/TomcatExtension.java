package arena.sei.gradle;

public class TomcatExtension {

  private String url = "http://www.strategylions.com.au/mirror/tomcat/tomcat-8/v8.5.43/bin/apache-tomcat-8.5.43.zip";

  private String[] cmd = new String[] {"bash", "bin/catalina.sh", "run"};

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String[] getCmd() {
    return cmd;
  }

  public void setCmd(String[] cmd) {
    this.cmd = cmd;
  }

}
