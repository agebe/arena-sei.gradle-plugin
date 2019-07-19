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

  public AddFrontendAction(Project project) {
    super();
    this.project = project;
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

  @Override
  public void execute(Task task) {
    //  File bdir = project.getBuildDir();
    //  System.out.println("build dir is: " + bdir.getAbsolutePath());
    //  project.getProperties().keySet().stream().sorted().forEachOrdered(k -> {
    //    Object v = project.getProperties().get(k);
    //    System.out.println(String.format("%s -> %s", k, v));
    //  });
    // TODO figure out how to this from the war plugin configuration
    // for now just make it an extra option
    File fWar = new File(project.getBuildDir(), "libs/arena-sei.dashboard-server.war");
    // TODO make this configurable
    // look in user home first, sibling directory next then give up
    File fromDirectory = new File("/home/uqageber/arena-sei.dashboard/build");
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
