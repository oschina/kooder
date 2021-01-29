package com.gitee.kooder.code;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.index.IndexException;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.storage.StorageFactory;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexNotFoundException;
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
            if (docs.totalHits.value == 0)
                return null;
            Document doc = reader.document(docs.scoreDocs[0].doc);
            return new CodeRepository().setDocument(doc);
        } catch (IndexNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new IndexException("Failed to get repo in metedata db : id = " + id, e);
        }
    }

    @Override
    public void save(CodeRepository repo) {
        synchronized (this){ //不支持并发写入
            try (IndexWriter writer = StorageFactory.getIndexWriter(Constants.TYPE_METADATA)) {
                Document doc = repo.getDocument();
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

}