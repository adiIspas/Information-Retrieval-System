package adrian.ispas.core.helper;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
public class TextFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}
