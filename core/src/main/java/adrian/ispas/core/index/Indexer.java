package adrian.ispas.core.index;

import adrian.ispas.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Indexed is used to extract documents from a specific path and index in system
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class Indexer {

    private static final Logger LOG = Logger.getLogger(Indexer.class);
    private IndexWriter writer;

    /**
     * @param indexDirectoryPath Path to directory where indexes will be stored
     * @throws IOException Path can't exist
     */
    public Indexer(String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Constants.Analyzer.getAnalyzer());

        writer = new IndexWriter(indexDirectory, indexWriterConfig);
    }

    /**
     * Prepare a document to be indexed
     * @param file Physical document
     * @return A document prepared for index
     * @throws IOException If physical document doesn't exist
     */
    private Document extractDocumentFrom(File file) throws IOException {
        Document document = new Document();
        String currentPath = FileSystems.getDefault().getPath("").toAbsolutePath().toString();

        document.add(new StringField(Constants.FILE_NAME, file.getName(), Field.Store.YES));
        document.add(new StringField(Constants.FILE_PATH, file.getAbsolutePath().replace(currentPath, ""), Field.Store.YES));
        document.add(new TextField(Constants.CONTENTS, extractContent(file), Field.Store.YES));

        return document;
    }

    /**
     * Index a physical document
     * @param file Physical document
     * @throws IOException If physical document doesn't exist
     */
    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+ file.getCanonicalPath());

        Document document = extractDocumentFrom(file);
        writer.addDocument(document);
    }

    /**
     * Create indexes from all documents from a folder
     * @param directoryPath Path to the directory with files
     * @param filters Filter for documents, types of accepted documents
     * @return Total number of documents indexed
     * @throws IOException If directory doesn't exist
     */
    public int createIndex(String directoryPath, List<FileFilter> filters) throws IOException {
        File[] files = new File(directoryPath).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (checkFileAvailability(file, filters)) {
                        indexFile(file);
                    }
                } else if (file.isDirectory()) {
                    createIndex(file.getAbsolutePath(), filters);
                }
            }
        } else {
            LOG.error("Your directory doesn't contain files.");
        }

        return writer.numDocs();
    }

    /**
     * Delete all old indexes
     * @param directoryPath Path to indexes directory
     * @throws IOException Path can't exist
     */
    public static void deleteIndexes(String directoryPath) throws IOException {
        File[] files = new File(directoryPath).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Files.delete(file.toPath());
                }
            }
        } else {
            LOG.error("Your directory doesn't contain files.");
        }
    }

    /**
     * Close a writer after indexed process
     */
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            LOG.error("ERROR: Writer can't be close because: " + e);
        }
    }

    /**
     * Check if a file is available to be indexed
     * @param file File that should be checked
     * @param filters A list of filters for files extension
     * @return Availability of file
     */
    private Boolean checkFileAvailability(File file, List<FileFilter> filters) {
        return !file.isDirectory() &&
                !file.isHidden() &&
                file.exists() &&
                file.canRead() &&
                filters.stream().anyMatch(t -> t.accept(file));
    }

    /**
     * Extract content from all supported types of files
     * @param file File for that is wanted to extracts it content
     * @return Content of file
     */
    private String extractContent(File file) {
        String fileContent = "";
        Tika tika = new Tika();

        try {
            return tika.parseToString(file);
        } catch (IOException | TikaException e) {
            LOG.error("Your file content can't be read because: " + e);
        }

        return fileContent;
    }
}
