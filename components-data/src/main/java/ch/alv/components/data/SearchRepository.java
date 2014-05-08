package ch.alv.components.data;

import ch.alv.components.core.search.ValuesProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/**
 * This component provides methods to execute searches within a certain data source.
 *
 * @since 1.0.0
 */
public interface SearchRepository<TYPE, ID extends Serializable> extends DataRepository<TYPE, ID> {

    /**
     * Find entities with the default search configuration.
     *
     * @param valuesProvider provides the parameters which should be used
     * @return a page of matching entities
     */
    Page<TYPE> find(ValuesProvider valuesProvider);

    /**
     * Find entities with the default search configuration and adapt the result list to be conform to the given pageable.
     *
     * @param valuesProvider provides the parameters which should be used.
     * @param pageable       paging information to be considered when executing the search.
     * @return a page of matching entities
     */
    Page<TYPE> find(ValuesProvider valuesProvider, Pageable pageable);

    /**
     * Find entities with the given search configuration.
     *
     * @param valuesProvider provides the parameters which should be used
     * @param searchName     the name of the search that should be used.
     * @return a page of matching entities
     */
    Page<TYPE> find(ValuesProvider valuesProvider, String searchName);

    /**
     * Find entities with the given search configuration and adapt the result list to be conform to the given pageable.
     *
     * @param valuesProvider provides the parameters which should be used.
     * @param pageable       paging information to be considered when executing the search.
     * @param searchName     the name of the search that should be used.
     * @return a page of matching entities
     */
    Page<TYPE> find(ValuesProvider valuesProvider, Pageable pageable, String searchName);

}
