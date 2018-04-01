package adrian.ispas.core.retrive;

/**
 * Processes Results is used to put the results of search in a specified format.
 *
 * Created by Adrian Ispas on Apr, 2018
 */
public class ProcessesResults {

    private ProcessesResults(){}

    /**
     * Make path like FOLDER > SUBFOLDER
     * @param path Path of file
     * @param pathDelimiter Delimiter for the system configuration
     * @param pathStarter Name of the main folder for raw documents
     * @return New path like FOLDER > SUBFOLDER
     */
    public static String processesPath(String path, String pathDelimiter, String pathStarter) {

        boolean isActive = false;
        StringBuilder processedPath = new StringBuilder();
        for(String currentLocation : path.split(pathDelimiter)) {
            if (currentLocation.equals(pathStarter)) {
                isActive = true;
            }

            if (isActive) {
                processedPath.append(" > ").append(currentLocation.toUpperCase());
            }
        }

        processedPath.deleteCharAt(processedPath.indexOf("> "));
        processedPath.delete(processedPath.lastIndexOf(" > "), processedPath.length());

        return processedPath.toString();
    }
}
