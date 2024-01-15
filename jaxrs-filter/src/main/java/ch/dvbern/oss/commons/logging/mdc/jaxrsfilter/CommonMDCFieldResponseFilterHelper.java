package ch.dvbern.oss.commons.logging.mdc.jaxrsfilter;

import ch.dvbern.oss.commons.logging.mdc.MDCDAO;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
public class CommonMDCFieldResponseFilterHelper {
	private final MDCResponseFilter responseFilterHelper;

	public static CommonMDCFieldResponseFilterHelper usingDefaults() {
		return new CommonMDCFieldResponseFilterHelper(
				MDCResponseFilter.usingRequestContextPropertyKey(
						CommonMDCFieldRequestFilterHelper.CONTEXT_KEY
				)
		);
	}

	public CommonMDCFieldResponseFilterHelper withMDCDAO(MDCDAO mdcdao) {
		return new CommonMDCFieldResponseFilterHelper(
				responseFilterHelper.withMdcDao(mdcdao)
		);
	}

	public void filter(
			ContainerRequestContext requestContext,
			// please keep this unused parameter. Removing it feels very weird on the caller side!
			@SuppressWarnings("unused")
			ContainerResponseContext responseContext
	) {
		responseFilterHelper.filter(requestContext, responseContext);
	}

}
