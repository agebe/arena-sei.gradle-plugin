package arena.sei.gradle.greeting;

public class GreetingPluginExtension {

  private String message = "hello";

  private String greeter = "sei";

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getGreeter() {
    return greeter;
  }

  public void setGreeter(String greeter) {
    this.greeter = greeter;
  }

}
