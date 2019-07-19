package arena.sei.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class SeiToolsPlugin implements Plugin<Project> {

  private final String SEI_TOOLS = "SEI Tools";

  @Override
  public void apply(Project project) {
    Task task = project.task("addFrontend");
    task.setDescription("add the static frontend to the backend war file");
    task.setGroup(SEI_TOOLS);
    task.dependsOn("war");
    task.doLast(new AddFrontendAction(project));

    GreetingPluginExtension extension = project.getExtensions().create("greeting", GreetingPluginExtension.class);
    Task greeting = project.task("greet");
    greeting.setDescription("hello world");
    greeting.setGroup(SEI_TOOLS);
    greeting.doLast(t -> {
      System.out.println(String.format("%s from %s", extension.getMessage(), extension.getGreeter()));
    });
  }

}
