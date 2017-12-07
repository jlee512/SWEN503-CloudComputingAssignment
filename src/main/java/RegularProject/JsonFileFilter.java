package RegularProject;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by Julian on 5/12/2017.
 */
public class JsonFileFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        } else{
            String filename = f.getName().toLowerCase();
            return filename.endsWith(".json");
        }
    }

    public String getDescription() {
        return "JSON files (*.json)";
    }

}
