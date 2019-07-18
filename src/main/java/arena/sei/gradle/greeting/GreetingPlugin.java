package arena.sei.gradle.greeting;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class GreetingPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    Task greeting = project.task("hello");
    greeting.setDescription("hello world");
    greeting.setGroup("SEI Tools");
    greeting.doLast(task -> {
      System.out.println("hello sei");
    });
  }

}
