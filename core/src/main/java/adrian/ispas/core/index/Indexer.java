package adrian.ispas.core.index;

import adrian.ispas.core.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Indexed is used to extract documents from a specific path and index in system
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class Indexer {

    private static final Logger LOG = Logger.getLogger(Indexer.class);
    private IndexWriter writer;

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

        document.add(new Field(Constants.FILE_NAME, file.getName(), TextField.TYPE_STORED));
        document.add(new Field(Constants.CONTENTS, new Scanner(file).useDelimiter("\\A").next(), TextField.TYPE_STORED));

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
     * @param filter Filter for documents, types of accepted documents
     * @return Total number of documents indexed
     * @throws IOException If directory doesn't exist
     */
    public int createIndex(String directoryPath, FileFilter filter) throws IOException {
        File[] files = new File(directoryPath).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (checkFileAvailability(file, filter)) {
                        indexFile(file);
                    }
                } else if (file.isDirectory()) {
                    createIndex(file.getAbsolutePath(), filter);
                }
            }
        } else {
            LOG.error("Your directory doesn't contain files.");
        }

        return writer.numDocs();
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
     * @param filter Filter for files
     * @return Availability of file
     */
    private Boolean checkFileAvailability(File file, FileFilter filter) {
        return !file.isDirectory() &&
                !file.isHidden() &&
                file.exists() &&
                file.canRead() &&
                filter.accept(file);
    }
}
