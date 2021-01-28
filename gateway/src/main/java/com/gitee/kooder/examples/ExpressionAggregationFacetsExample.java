package com.gitee.kooder.examples;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;


/** Shows facets aggregation by an expression. */
public class ExpressionAggregationFacetsExample {

    private static Directory indexDir = null;
    private static Directory taxoDir = null;
    private static FacetsConfig config = new FacetsConfig();

    /** Empty constructor */
    public ExpressionAggregationFacetsExample() {}

    /** Build the example index. */
    private void index() throws IOException {
        IndexWriter indexWriter = new IndexWriter(indexDir, new IndexWriterConfig().setOpenMode(OpenMode.CREATE_OR_APPEND));

        // Writes facet ords to a separate directory from the main index
        DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

        Document doc = new Document();
        doc.add(new StringField("id", "1", Store.NO));
        doc.add(new TextField("c", "foo bar", Store.NO));
        doc.add(new NumericDocValuesField("popularity", 5L));
        doc.add(new FacetField("lang", "Java"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new StringField("id", "2", Store.NO));
        doc.add(new TextField("c", "foo foo bar", Store.NO));
        doc.add(new NumericDocValuesField("popularity", 3L));
        doc.add(new FacetField("lang", "Java"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new StringField("id", "3", Store.NO));
        doc.add(new TextField("c", "第三个 foo bar", Store.NO));
        doc.add(new NumericDocValuesField("popularity", 3L));
        doc.add(new FacetField("lang", "PHP"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new StringField("id", "4", Store.NO));
        doc.add(new TextField("c", "第四个 foo bar", Store.NO));
        doc.add(new NumericDocValuesField("popularity", 3L));
        doc.add(new FacetField("lang", "C#"));
        indexWriter.addDocument(config.build(taxoWriter, doc));

        indexWriter.close();
        taxoWriter.close();
    }


    /** Build the example index. */
    private void delete() throws IOException {
        IndexWriter indexWriter = new IndexWriter(indexDir, new IndexWriterConfig().setOpenMode(OpenMode.CREATE_OR_APPEND));
        indexWriter.deleteDocuments(new Term("id", "3"));
        indexWriter.close();
    }

    /** User runs a query and aggregates facets. */
    private FacetResult search() throws IOException, ParseException {
        DirectoryReader indexReader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDir);

        // Aggregates the facet values
        FacetsCollector fc = new FacetsCollector(false);

        // MatchAllDocsQuery is for "browsing" (counts facets
        // for all non-deleted docs in the index); normally
        // you'd use a "normal" query:
        FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);

        // Retrieve results
        //Facets facets = new TaxonomyFacetSumValueSource(taxoReader, config, fc, expr.getDoubleValuesSource(bindings));

        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        FacetResult result = facets.getTopChildren(10, "lang");

        indexReader.close();
        taxoReader.close();

        return result;
    }

    /** Runs the search and drill-down examples and prints the results. */
    public static void main(String[] args) throws Exception {
        try {
            taxoDir = NIOFSDirectory.open(Paths.get("D:\\lucene_taxo"));
            indexDir = NIOFSDirectory.open(Paths.get("D:\\lucene_index"));
        } catch (IOException e ){}

        ExpressionAggregationFacetsExample app = new ExpressionAggregationFacetsExample();
        app.index();

        FacetResult result = app.search();
        System.out.println(result);

        System.out.println("----------test delete---------");
        app.delete();
        result = app.search();
        System.out.println(result);
    }
}