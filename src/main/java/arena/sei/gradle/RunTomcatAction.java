package arena.sei.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;

public class RunTomcatAction implements Action<Task> {

  private Project project;

  private TomcatExtension tomcat;

  public RunTomcatAction(Project project, TomcatExtension tomcat) {
    super();
    this.project = project;
    this.tomcat = tomcat;
  }

  private File warFile() {
      Task war = project.getTasks().getByName("war");
      if(war == null) {
        throw new RuntimeException("war task not found");
      }
      return war.getOutputs().getFiles().getSingleFile();
  }

  private File userTomcat() {
    File fUserHome = new File(System.getProperty("user.home"));
    return new File(fUserHome, ".arena-sei/gradle/tomcat");
  }

  private File userTomcatProject() {
    return new File(userTomcat(), project.getName());
  }

  private File tomcatZipFile() {
    try {
      URL url = new URL(tomcat.getUrl());
      String filename = FilenameUtils.getName(url.getPath());
      File f = new File(userTomcat(), filename);
      if(f.exists()) {
        return f;
      } else {
        System.out.println("downloading tomcat from " + url.toString());
        try(InputStream in = url.openStream()) {
          FileUtils.copyInputStreamToFile(in, f);
        }
        return f;
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private File tomcatHome() {
    return new File(project.getBuildDir(), "tomcat");
  }

  private File catalinaHome() {
    File f = tomcatHome();
    File[] files =  f.listFiles(pathname -> pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat"));
    if(files.length >= 1) {
      return files[0];
    } else if(files.length == 0) {
      File[] files2 =  f.listFiles(pathname -> pathname.isDirectory());
      if(files2.length > 0) {
        return files2[0];
      }
    }
    throw new RuntimeException("failed to determine catalina home");
  }

  @Override
  public void execute(Task task) {
    File fWar = warFile();
    File fTomcatHome = tomcatHome();
    project.mkdir(fTomcatHome);
    FileTree zipTree = project.zipTree(tomcatZipFile());
    project.copy(c -> {
      c.from(zipTree);
      c.into(fTomcatHome);
    });
    File fCatalinaHome = catalinaHome();
    File fWebapps = new File(fCatalinaHome, "webapps");
    try {
      FileUtils.cleanDirectory(fWebapps);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    project.copy(c -> {
      c.from(fWar);
      c.into(fWebapps);
      c.rename(fWar.getName(), "ROOT.war");
    });
    File fUserTomcatProject = userTomcatProject();
    if(fUserTomcatProject.exists() && fUserTomcatProject.isDirectory()) {
      project.copy(c -> {
        c.from(fUserTomcatProject);
        c.into(fCatalinaHome);
      });
    }
    run(fCatalinaHome);
  }

  private void run(File fCatalinaHome) {
    // tried with ProcessBuilder.inheritIO but it somehow doesn't work with gradle so falling back to StreamGobbler
    ProcessBuilder pb = new ProcessBuilder(tomcat.getCmd());
    pb.directory(fCatalinaHome);
    try {
      Process tomcat = pb.start();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        tomcat.destroy();
      }));
      Thread tOut = new Thread(new StreamGobbler(tomcat.getInputStream(), System.out::println));
      tOut.setDaemon(true);
      tOut.start();
      Thread tErr = new Thread(new StreamGobbler(tomcat.getErrorStream(), System.err::println));
      tErr.setDaemon(true);
      tErr.start();
      tomcat.waitFor();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}
