package ch.dvbern.oss.commons.logging.mdc.jaxrsfilter;

import java.util.Optional;

import ch.dvbern.oss.commons.logging.mdc.MDCDAO;
import ch.dvbern.oss.commons.logging.mdc.MDCDAODefaultImpl;
import ch.dvbern.oss.commons.logging.mdc.MDCValues;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MDCResponseFilter {
	@With
	private final MDCDAO mdcDao;
	private final String requestContextKey;

	/**
	 * @param requestContextKey See {@link MDCRequestFilter#usingRequestContextPropertyKey(String)}
	 */
	public static MDCResponseFilter usingRequestContextPropertyKey(String requestContextKey) {
		return new MDCResponseFilter(new MDCDAODefaultImpl(), requestContextKey);
	}

	public void filter(
			ContainerRequestContext requestContext,
			// please keep this unused parameter. Removing it feels very weird on the caller side!
			@SuppressWarnings("unused")
			ContainerResponseContext responseContext
	) {
		Optional.ofNullable((MDCValues) requestContext.getProperty(CommonMDCFieldRequestFilterHelper.CONTEXT_KEY))
				.ifPresent(mdcDao::removeFromMDC);
	}
}
