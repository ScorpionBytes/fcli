package com.fortify.cli.common.rest.paging;

import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import kong.unirest.HttpRequest;
import kong.unirest.PagedList;

@SuppressWarnings("unchecked") // TODO Can we get rid of these warnings in a better way?
public class PagingHelper {
    private static final Pattern linkHeaderPattern = Pattern.compile("<([^>]*)>; *rel=\"([^\"]*)\"");
    
    public static final PagedList<JsonNode> pagedRequest(HttpRequest<?> request, INextPageUrlProducer nextPageUrlProducer) {
        return pagedRequest(request, nextPageUrlProducer, JsonNode.class);
    }
    
    public static final <R extends JsonNode> PagedList<R> pagedRequest(HttpRequest<?> request, INextPageUrlProducer nextPageUrlProducer, Class<R> returnType) {
        return request.asPaged(r->r.asObject(returnType), nextPageUrlProducer::getNextPageUrl);
    }
    
    public static final INextPageUrlProducer linkHeaderNextPageUrlProducer(String headerName, String relName) {
        return r -> {
            String linkHeader = r.getHeaders().getFirst(headerName);
            Optional<String> nextLink = linkHeaderPattern.matcher(linkHeader).results()
                .filter(r1->relName.equals(r1.group(2)))
                .findFirst()
                .map(r2->r2.group(1));
            return nextLink.orElse(null);
        };
    }
}
