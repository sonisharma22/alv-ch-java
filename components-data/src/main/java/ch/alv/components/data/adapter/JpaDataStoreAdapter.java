package ch.alv.components.data.adapter;

import ch.alv.components.core.beans.Identifiable;
import ch.alv.components.core.search.ValuesProvider;
import ch.alv.components.data.DataLayerException;
import ch.alv.components.data.query.NoSuchQueryProviderException;
import ch.alv.components.data.query.QueryFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA implementation of the {@link DataStoreAdapter} interface.
 *
 * @since 1.0.0
 */
public class JpaDataStoreAdapter<TYPE extends Identifiable<ID>, ID extends Serializable> implements DataStoreAdapter<TYPE, ID> {

    @PersistenceContext
    private EntityManager em;

    private final QueryFactory queryFactory;

    private final Map<String, Object> factoryServices = new HashMap<>();

    public JpaDataStoreAdapter(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @PostConstruct
    private void init() {
        factoryServices.put("entityManager", em);
    }

    @Override
    @Transactional
    public TYPE save(TYPE entity, Class<TYPE> entityClass) throws DataLayerException {
        if (entity.getId() != null) {
            em.find(entityClass, entity.getId());
        }
        TYPE newEntity = em.merge(entity);
        return newEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public TYPE find(ID id, Class<TYPE> entityClass) throws DataLayerException {
        return em.find(entityClass, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TYPE> find(String queryName, ValuesProvider params, Class<TYPE> entityClass) throws DataLayerException {
        try {
            String queryString = queryFactory.createQuery(queryName, params, factoryServices, entityClass);
            TypedQuery<TYPE> query = em.createQuery(queryString, entityClass);
            return query.getResultList();
        } catch (NoSuchQueryProviderException e) {
            throw new DataLayerException("Could not execute query with name '" + queryName + "'.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TYPE> find(Class<TYPE> entityClass) throws DataLayerException {
        String name = entityClass.getSimpleName();
        String token = name.substring(0, 1).toLowerCase();
        return em.createQuery("select " + token + " from " + name + " " + token, entityClass).getResultList();
    }

    @Override
    @Transactional
    public void delete(ID id, Class<TYPE> entityClass) throws DataLayerException {
        Object entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
    }
}
