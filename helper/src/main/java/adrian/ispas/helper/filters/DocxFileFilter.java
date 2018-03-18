package adrian.ispas.helper.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
public class DocxFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".docx");
    }
}
