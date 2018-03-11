package adrian.ispas.core.index;

import adrian.ispas.core.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
public class Indexer {

    private static final Logger LOG = Logger.getLogger(Indexer.class);
    private IndexWriter writer;

    // Init index writer object
    public Indexer(String indexDirectoryPath) throws IOException {
        // Set directory, analyzer and configuration
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
//        Directory indexDirectory = new RAMDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

        // Create writer
        writer = new IndexWriter(indexDirectory, indexWriterConfig);
    }

    private Document extractDocumentFrom(File file) throws IOException {
        Document document = new Document();

        document.add(new Field(Constants.FILE_NAME, file.getName(), TextField.TYPE_STORED));
        document.add(new Field(Constants.CONTENTS, new Scanner(file).useDelimiter("\\A").next(), TextField.TYPE_STORED));

        return document;
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+ file.getCanonicalPath());
        Document document = extractDocumentFrom(file);
        writer.addDocument(document);
    }

    public int createIndex(String directoryPath, FileFilter filter) throws IOException {
        File[] files = new File(directoryPath).listFiles();

        for (File file:files) {
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
                indexFile(file);
            }
        }

        return writer.numDocs();
    }

    // Close the index writer
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            LOG.error("ERROR: Writer can't be close because: " + e);
        }
    }
}
