package com.gitee.search.code;

import com.gitee.search.core.Constants;
import com.gitee.search.index.IndexException;
import com.gitee.search.storage.StorageFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

/**
 * 用于管理源代码仓库元信息
 * @author Winter Lau<javayou@gmail.com>
 */
public interface RepositoryManager {

    RepositoryManager INSTANCE = new LuceneRepositoryManager();

    /**
     * 根据仓库的编号获取仓库元信息
     * @param id
     * @return
     */
    CodeRepository get(long id);

    /**
     * 保存仓库元信息，如果存在 id 相同的则覆盖更新
     * @param repo
     */
    void save(CodeRepository repo);

    /**
     * 删除元信息
     * @param id
     * @return
     */
    boolean delete(long id);

}

/**
 * 使用 Lucene 索引库来保存代码仓库元信息
 * @author Winter Lau<javayou@gmail.com>
 */
class LuceneRepositoryManager implements RepositoryManager {

    @Override
    public CodeRepository get(long id) {
        try (IndexReader reader = StorageFactory.getIndexReader(Constants.TYPE_METADATA)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(new TermQuery(new Term(Constants.FIELD_REPO_ID, String.valueOf(id))), 1);
            if(docs.totalHits.value == 0)
                return null;
            Document doc = reader.document(docs.scoreDocs[0].doc);
            return readRepository(doc);
        } catch (IOException e) {
            throw new IndexException("Failed to get repo in metedata db : id = " + id, e);
        }
    }

    @Override
    public void save(CodeRepository repo) {
        synchronized (this){ //不支持并发写入
            try (IndexWriter writer = StorageFactory.getIndexWriter(Constants.TYPE_METADATA)) {
                Document doc = buildDocument(repo);
                writer.updateDocument(new Term(Constants.FIELD_REPO_ID, repo.getIdAsString()), doc);
            } catch (IOException e) {
                throw new IndexException("Failed to save repo in metedata db : " + repo, e);
            }
        }
    }

    @Override
    public boolean delete(long id) {
        synchronized (this) {
            try (IndexWriter writer = StorageFactory.getIndexWriter(Constants.TYPE_METADATA)) {
                writer.deleteDocuments(new Term(Constants.FIELD_REPO_ID, String.valueOf(id)));
            } catch (IOException e) {
                throw new IndexException("Failed to delete repo from metedata db, id = " + id, e);
            }
            return false;
        }
    }

    protected Document buildDocument(CodeRepository repo) {
        Document doc = new Document();
        doc.add(new StringField(Constants.FIELD_REPO_ID,        repo.getIdAsString(), Field.Store.YES));
        doc.add(new StoredField(Constants.FIELD_REPO_URL,       repo.getUrl()));
        doc.add(new StoredField(Constants.FIELD_REPO_NAME,      repo.getName()));
        if(repo.getLastCommitId() != null)
            doc.add(new StoredField(Constants.FIELD_REVISION,   repo.getLastCommitId()));
        if(repo.getScm() != null)
            doc.add(new StoredField(Constants.FIELD_SCM,        repo.getScm()));
        return doc;
    }

    protected CodeRepository readRepository(Document doc) {
        CodeRepository repo = new CodeRepository();
        repo.setId(NumberUtils.toLong(doc.get(Constants.FIELD_REPO_ID), 0));
        repo.setName(doc.get(Constants.FIELD_REPO_NAME));
        repo.setUrl(doc.get(Constants.FIELD_REPO_URL));
        repo.setLastCommitId(doc.get(Constants.FIELD_REVISION));
        repo.setScm(doc.get(Constants.FIELD_SCM));
        return repo;
    }

}