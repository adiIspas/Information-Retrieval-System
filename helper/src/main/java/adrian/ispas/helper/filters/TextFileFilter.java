package adrian.ispas.helper.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter used to select just '.txt' documents
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class TextFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}
