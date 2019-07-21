package arena.sei.gradle;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class AddFrontendAction implements Action<Task> {

  private Project project;

  private AddFrontendExtension addFrontendExtension;

  public AddFrontendAction(Project project, AddFrontendExtension addFrontendExtension) {
    super();
    this.project = project;
    this.addFrontendExtension = addFrontendExtension;
  }

  private List<File> listRecursive(File dir, List<File> l) {
    File[] c = dir.listFiles();
    if(c != null) {
      Arrays.stream(c).forEachOrdered(f -> {
        if(f.isDirectory()) {
          listRecursive(f, l);
        } else {
          l.add(f);
        }
      });
    }
    return l;
  }

  private List<File> listRecursive(File dir) {
    return listRecursive(dir, new ArrayList<>());
  }

  private File frontendBuild() {
    File f;
    if(addFrontendExtension.getFrontendBuild() != null) {
      f = new File(addFrontendExtension.getFrontendBuild());
      if(f.exists() && f.isDirectory()) {
        return f;
      } else {
        throw new RuntimeException("not directory " + f.getAbsolutePath());
      }
    } else {
      File home = new File(System.getProperty("user.home"));
      f = new File(home, "arena-sei.dashboard/build");
      if(f.exists() && f.isDirectory()) {
        return f;
      } else {
        throw new RuntimeException("failed to locate frontend build directory,"
            + " please specify in build.gradle (frontend.frontendBuild)");
      }
    }
  }

  private File warFile() {
    if(addFrontendExtension.getBackendWar() != null) {
      return project.file(addFrontendExtension.getBackendWar());
    } else {
      Task war = project.getTasks().getByName("war");
      if(war == null) {
        throw new RuntimeException("war task not found");
      }
      return war.getOutputs().getFiles().getSingleFile();
//      return new File(project.getBuildDir(), "libs/arena-sei.dashboard-server.war");
    }
  }

  @Override
  public void execute(Task task) {
    //  File bdir = project.getBuildDir();
    //  System.out.println("build dir is: " + bdir.getAbsolutePath());
//    project.getProperties().keySet().stream().sorted().forEachOrdered(k -> {
//      Object v = project.getProperties().get(k);
//      System.out.println(String.format("%s -> %s", k, v));
//    });
//    Task war = (Task)project.getProperties().get("war");
//    System.out.println(war.property("archiveFile"));
//    System.out.println(project.file(war.property("archiveFile")));
//    System.out.println(project.getTasks().getByName("war").property("archiveName"));
//    System.out.println(project.getTasks().getByName("war").property("archiveFile"));
//    System.out.println(war.getOutputs().getFiles().getSingleFile());
    File fWar = warFile();
    File fromDirectory = frontendBuild();
    List<File> toCopy = listRecursive(fromDirectory);
    // from https://stackoverflow.com/a/17504151
    Map<String, String> env = new HashMap<>(); 
    Path path = fWar.toPath();
    URI uri = URI.create("jar:" + path.toUri());
    try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
      toCopy.forEach(f -> {
        Path nf = fs.getPath(f.getAbsolutePath().substring((int) fromDirectory.getAbsolutePath().length()+1));
        Path parent = nf.getParent();
        if((parent != null) && Files.notExists(parent)) {
          project.getLogger().debug("create parent {}", parent);
          try {
            Files.createDirectories(parent);
          } catch(Exception e) {
            throw new RuntimeException("failed to create parent folder " + parent, e);
          }
        }
        project.getLogger().debug("copy {} to {}", f.getAbsolutePath(), nf);
        try(OutputStream out = Files.newOutputStream(nf, StandardOpenOption.CREATE_NEW)) {
          Files.copy(f.toPath(), out);
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}
