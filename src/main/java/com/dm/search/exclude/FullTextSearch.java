package com.dm.search.exclude;

import com.dm.search.exclude.exception.SearchFailedException;
import com.dm.search.preprocessor.SLProcessor;
import com.dm.search.preprocessor.SearchParameters;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.stream;

@Repository
@Transactional

public class FullTextSearch<T> {

    private Class entityType;
    private FullTextEntityManager fullTextEm;
    private MultiFieldQueryParser queryParser;

    @Autowired
    public FullTextSearch(EntityManagerFactory emf) {
        this.fullTextEm = Search.getFullTextEntityManager(emf.createEntityManager());
    }

    public List<T> search(String query) throws SearchFailedException {
        return search(new SearchParameters.Builder(query).build());
    }

    public int count(String query) throws SearchFailedException {
        return count(new SearchParameters.Builder(query).build());
    }

    public List<T> search(SearchParameters sp) throws SearchFailedException {
        return parseQuery(sp).getResultList();
    }

    public int count(SearchParameters sp) throws SearchFailedException {
        return parseQuery(sp).getResultSize();
    }

    private FullTextQuery parseQuery(SearchParameters parameters) throws SearchFailedException {

        String query = parameters.getQuery();
        String filter = parameters.getFilter();
        Pageable p = parameters.getPageable();
        Assert.notNull(query, "Query must not be null");
        parameters.setFuzziness(0);



//        QueryBuilder qb = fullTextEm.getSearchFactory()
//                .buildQueryBuilder().forEntity(entityType).get();
//
//        Query fuzzyQuery = qb.keyword()
//                .onFields("sectionSet.id")
//                .matching("31917")
//                .createQuery();
//
//
//        FullTextQuery jpaQuery1 = fullTextEm.createFullTextQuery(fuzzyQuery, entityType);
//
////        Object o = jpaQuery1.getResultList();
////
////        System.out.println();


        try {
            query = new SLProcessor(parameters.getFuzziness()).format(query);
            query = (!filter.trim().isEmpty()) ? "(" + query + ") AND " + filter : query;

            query = "sectionSet.id:31917";
            Query r = queryParser.parse(query);
            FullTextQuery jpaQuery = fullTextEm.createFullTextQuery(r, entityType);


            Object o1 = jpaQuery.setMaxResults(p.getPageSize()).setFirstResult(p.getPageNumber() * p.getPageSize()).getResultList();
            return jpaQuery.setMaxResults(p.getPageSize()).setFirstResult(p.getPageNumber() * p.getPageSize());

        } catch (ParseException e) {
            throw new SearchFailedException(e.getMessage().split("\\v")[0].replace("~", ""), e);
        }
    }

    public void setEntityType(Class entityType) {
        Assert.notNull(entityType, "Entity type must not be null");
        queryParser = new MultiFieldQueryParser(getEntityFields(entityType), fullTextEm.getSearchFactory().getAnalyzer(entityType));
        queryParser.setAllowLeadingWildcard(true);
        this.entityType = entityType;
    }

    private String[] getEntityFields(Class c) {
        Set<String> fieldNames = new HashSet<>();
        while (c != null) {
            stream(c.getDeclaredFields()).map(Field::getName).forEach(fieldNames::add);
            c = c.getSuperclass();
        }
        return fieldNames.toArray(new String[0]);
    }
}


