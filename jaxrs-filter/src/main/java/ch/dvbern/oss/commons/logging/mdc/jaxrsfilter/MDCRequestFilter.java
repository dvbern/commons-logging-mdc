package ch.dvbern.oss.commons.logging.mdc.jaxrsfilter;

import ch.dvbern.oss.commons.logging.mdc.MDCDAO;
import ch.dvbern.oss.commons.logging.mdc.MDCDAODefaultImpl;
import ch.dvbern.oss.commons.logging.mdc.MDCValues;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MDCRequestFilter {
	@With
	private final MDCDAO mdcDao;
	private final String requestContextKey;

	/**
	 * @param requestContextKey The unique key to temporarily store the MDCValues using
	 * {@link ContainerRequestContext#setProperty(String, Object)}.
	 */
	public static MDCRequestFilter usingRequestContextPropertyKey(String requestContextKey) {
		return new MDCRequestFilter(new MDCDAODefaultImpl(), requestContextKey);
	}

	public void filter(
			ContainerRequestContext requestContext,
			MDCValues mdcValues
	) {
		requestContext.setProperty(requestContextKey, mdcValues);

		mdcDao.applyToMDC(mdcValues);
	}

}
