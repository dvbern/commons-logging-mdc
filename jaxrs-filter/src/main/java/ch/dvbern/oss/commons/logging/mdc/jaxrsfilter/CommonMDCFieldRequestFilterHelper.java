package ch.dvbern.oss.commons.logging.mdc.jaxrsfilter;

import ch.dvbern.oss.commons.logging.mdc.LoggingApp;
import ch.dvbern.oss.commons.logging.mdc.LoggingPrincipal;
import ch.dvbern.oss.commons.logging.mdc.LoggingSource;
import ch.dvbern.oss.commons.logging.mdc.LoggingTenant;
import ch.dvbern.oss.commons.logging.mdc.MDCDAO;
import ch.dvbern.oss.commons.logging.mdc.MDCValues;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonMDCFieldRequestFilterHelper {

	static final String CONTEXT_KEY = CommonMDCFieldRequestFilterHelper.class.getName() + ".CONTEXT_KEY";
	private final MDCRequestFilter requestFilterHelper;

	public static CommonMDCFieldRequestFilterHelper usingDefaults() {
		return new CommonMDCFieldRequestFilterHelper(
				MDCRequestFilter.usingRequestContextPropertyKey(
						CommonMDCFieldRequestFilterHelper.CONTEXT_KEY
				)
		);
	}

	public CommonMDCFieldRequestFilterHelper withMDCDAO(MDCDAO mdcdao) {
		return new CommonMDCFieldRequestFilterHelper(
				requestFilterHelper.withMdcDao(mdcdao)
		);
	}

	public void filter(
			ContainerRequestContext requestContext,
			LoggingApp appInfo,
			LoggingSource requestSource,
			@Nullable LoggingPrincipal principal,
			@Nullable LoggingTenant tenant
	) {

		var mdcValues = MDCValues.fromStandardFields(
				appInfo,
				requestSource,
				principal,
				tenant
		);

		requestFilterHelper.filter(requestContext, mdcValues);
	}

}
