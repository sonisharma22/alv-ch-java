package ch.alv.components.web.controller;

import ch.alv.components.core.model.ModelItem;
import ch.alv.components.core.search.ValuesProvider;
import ch.alv.components.core.spring.context.DefaultContextProvider;
import ch.alv.components.core.utils.StringHelper;
import ch.alv.components.service.search.SearchService;
import ch.alv.components.web.WebConstant;
import ch.alv.components.web.dto.Dto;
import ch.alv.components.web.dto.DtoFactory;
import ch.alv.components.web.endpoint.Endpoint;
import ch.alv.components.web.endpoint.EndpointRegistry;
import ch.alv.components.web.endpoint.filter.UnSupportedMethodException;
import ch.alv.components.web.endpoint.filter.UnauthorizedException;
import ch.alv.components.web.search.WebValuesProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Default controller for search requests (may be GET or POST). It has to be used with the alv-ch-web architecture..
 *
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SearchControllerImpl extends BaseController implements SearchController {

    @RequestMapping(method = RequestMethod.GET, value = "/{moduleName}/{storeName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object handleGetRequest(HttpServletRequest request,
                                @PathVariable String moduleName,
                                @PathVariable String storeName) throws UnauthorizedException, UnSupportedMethodException, NoSuchValuesProviderException {

        runFilters(request, moduleName, storeName);
        return find(createPageable(request), request.getParameterMap(), moduleName, storeName);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{moduleName}/{storeName}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object handleGetRequest(HttpServletRequest request,
                                @PathVariable String moduleName,
                                @PathVariable String storeName,
                                @PathVariable String id) throws UnSupportedMethodException, UnauthorizedException {

        runFilters(request, moduleName, storeName);
        return getById(storeName, moduleName, id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{moduleName}/{storeName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object handlePostRequest(HttpServletRequest request,
                                   @PathVariable String moduleName,
                                   @PathVariable String storeName,
                                   @RequestBody String body) throws UnauthorizedException, UnSupportedMethodException, NoSuchValuesProviderException {

        runFilters(request, moduleName, storeName);
        return find(createPageable(request), request.getParameterMap(), moduleName, storeName);
    }

    protected ResponseEntity<?> find(Pageable pageable, Map<String, String[]> params, String moduleName, String storeName) throws NoSuchValuesProviderException {
        Endpoint endpoint = EndpointRegistry.getEndpoint(moduleName, storeName);
        if (params.isEmpty()) {
            return handleGetAllSearch(pageable, endpoint);
        }

        String searchName = extractSearchName(params);
        if (StringHelper.isEmpty(searchName)) {
            return handleDefaultSearch(pageable, params, endpoint);
        } else {
            return handleNamedSearch(pageable, params, endpoint, searchName);
        }
    }

    protected String extractSearchName(Map<String, String[]> params) {
        String[] searchNameValues = params.get(WebConstant.PARAM_NAME_SEARCH);
        String searchName = null;
        if (searchNameValues != null && searchNameValues.length > 0) {
            searchName = searchNameValues[0];
        }
        return searchName;
    }

    protected ResponseEntity<?> handleGetAllSearch(Pageable pageable, Endpoint endpoint) throws NoSuchValuesProviderException {
        SearchService searchService = DefaultContextProvider.getBeanByName(endpoint.getServiceName());
        Page page = searchService.getAll(pageable);
        return new ResponseEntity<>(new PageImpl(convertEntityListToDtoList(page.getContent(), endpoint), pageable, page.getTotalElements()), HttpStatus.OK);
    }

    protected ResponseEntity<?> handleDefaultSearch(Pageable pageable, Map<String, String[]> params, Endpoint endpoint) throws NoSuchValuesProviderException {
        Page page = getService(endpoint).find(pageable, initValuesProvider(endpoint, params));
        return new ResponseEntity<>(new PageImpl(convertEntityListToDtoList(page.getContent(), endpoint), pageable, page.getTotalElements()), HttpStatus.OK);
    }

    protected ResponseEntity<?> handleNamedSearch(Pageable pageable, Map<String, String[]> params, Endpoint endpoint, String searchName) throws NoSuchValuesProviderException {
        ValuesProvider provider = initValuesProvider(endpoint, params);
        Page page = getService(endpoint).find(pageable, searchName, provider);
        return new ResponseEntity<>(new PageImpl(convertEntityListToDtoList(page.getContent(), endpoint), pageable, page.getTotalElements()), HttpStatus.OK);
    }

    protected ValuesProvider initValuesProvider(Endpoint endpoint, Map<String, String[]> params) throws NoSuchValuesProviderException {
        Class<? extends ValuesProvider> providerClass = endpoint.getValuesProviderClass();
        try {
            ValuesProvider provider;
            provider = providerClass.newInstance();
            if (provider instanceof WebValuesProvider) {
                ((WebValuesProvider) provider).setSource(params);
            }
            return provider;
        } catch (Exception e) {
            throw new NoSuchValuesProviderException("Error while executing search: No valuesProvider of class '" + providerClass.getName() + "' found.");
        }
    }

    protected Object getById(String moduleName, String storeName, String id) {
        Endpoint endpoint = EndpointRegistry.getEndpoint(moduleName, storeName);
        ModelItem entity;
        entity = ((SearchService) getBean(endpoint.getServiceName())).getById(id);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        DtoFactory dtoFactory = getBean("dtoFactory");
        return dtoFactory.createDtoFromEntity(entity, endpoint);
    }

    protected Dto convertEntityToDto(ModelItem entity, Endpoint endpoint) {
        return getDtoFactory().createDtoFromEntity(entity, endpoint);
    }

    protected List<Dto> convertEntityListToDtoList(List<ModelItem> entities, Endpoint endpoint) {
        List<Dto> dtos = new ArrayList<>();
        for (ModelItem entity : entities) {
            dtos.add(convertEntityToDto(entity, endpoint));
        }
        return dtos;
    }

    protected SearchService getService(Endpoint endpoint) {
        return DefaultContextProvider.getBeanByName(endpoint.getServiceName());
    }

}