package com.eldrix.termsearch;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.index.CorruptIndexException;

import com.eldrix.terminology.snomedct.Search;
import com.eldrix.terminology.snomedct.Search.ResultItem;
import com.google.inject.Inject;

import io.bootique.cli.Cli;

@Path("snomedct")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
	private static final String ERROR_NO_SEARCH_PARAMETER = "No search parameter specified";

	@Context
	private Configuration config;

	@Inject
	private Cli cli;
	
	/**
	 * Search for a concept using the search terms provided.
	 * @param search - search term
	 * @param root - one or more root concept identifiers
	 * @param is - zero or more direct parent concept identifiers
	 * @param maxHits - number of hits
	 * @param fsn - whether to include FSN terms in search results (defaults to 0)
	 * @param inactive - whether to include inactive terms in search results (defaults to 0)
	 * @param fuzzy - whether to use a fuzzy search for search (default to false)
	 * @param fallbackFuzzy - whether to use a fuzzy search if no results found for non-fuzzy search (defaults to true)
	 * @param project - optional name of project to limit search results to curated list for that project
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("search")
	public List<ResultItem> search(@QueryParam("s") String search,
			@DefaultValue("138875005") @QueryParam("root") final List<Long> recursiveParents,
			@QueryParam("is") final List<Long> directParents,
			@QueryParam("refset") final List<Long> refsets,
			@DefaultValue("200") @QueryParam("maxHits") int maxHits,
			@DefaultValue("false") @QueryParam("fsn") boolean includeFsn,
			@DefaultValue("false") @QueryParam("inactive") boolean includeInactive,
			@DefaultValue("false") @QueryParam("fuzzy") boolean fuzzy,
			@DefaultValue("true") @QueryParam("fallbackFuzzy") boolean fallbackFuzzy,
			@Context UriInfo uriInfo) {
		if (search == null || search.length() == 0) {
			throw new BadRequestException(ERROR_NO_SEARCH_PARAMETER);
		}
		try {
			return _performSearch(search, recursiveParents, directParents, refsets, maxHits, includeFsn,
					includeInactive, fuzzy, fallbackFuzzy);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getLocalizedMessage());
		}
	}

	private List<ResultItem> _performSearch(String search, final List<Long> recursiveParents,
			final List<Long> directParents, final List<Long> refsets, int maxHits, boolean includeFsn, boolean includeInactive, boolean fuzzy,
			boolean fallbackFuzzy) throws CorruptIndexException, IOException {
		Search.Request.Builder b = Search.getInstance(cli.optionString(Application.OPTION_INDEX)).newBuilder();
		b.setMaxHits(maxHits)
		.withRecursiveParent(recursiveParents);
		if (search != null && search.length() > 0) {
			b.search(search);
		}
		if (!includeInactive) {
			b.onlyActive();
		}
		if (!includeFsn) {
			b.withoutFullySpecifiedNames();
		}
		if (fuzzy) {
			b.useFuzzy();
		}
		if (directParents.size() > 0) {
			b.withDirectParent(directParents);
		}
		if (refsets.size() > 0) {
			b.inRefsets(refsets);
		}
		List<ResultItem> result = b.build().search();
		if (!fuzzy && fallbackFuzzy && result.size() == 0) {
			result = b.useFuzzy().build().search();
		}
		return result;
	}

}
