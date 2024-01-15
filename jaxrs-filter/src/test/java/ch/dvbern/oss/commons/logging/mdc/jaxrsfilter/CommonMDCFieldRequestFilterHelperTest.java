package ch.dvbern.oss.commons.logging.mdc.jaxrsfilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.oss.commons.logging.mdc.CommonMDCField;
import ch.dvbern.oss.commons.logging.mdc.LoggingApp;
import ch.dvbern.oss.commons.logging.mdc.LoggingPrincipal;
import ch.dvbern.oss.commons.logging.mdc.LoggingSource;
import ch.dvbern.oss.commons.logging.mdc.LoggingTenant;
import ch.dvbern.oss.commons.logging.mdc.MDCDAO;
import ch.dvbern.oss.commons.logging.mdc.MDCValues;
import com.vdurmont.semver4j.Semver;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class CommonMDCFieldRequestFilterHelperTest {
	@Mock
	ContainerRequestContext requestCtx;

	private final Map<String, Object> requestContextProperties = new HashMap<>();

	// SLF4j SimpleLogger does not support MDC and thus stored values are always null so we have to fake it :(
	static class FakeMDCDAO implements MDCDAO {
		private final Map<String, Object> fakeMDC = new HashMap<>();

		@Override
		public void applyToMDC(MDCValues mdcValues) {
			mdcValues.values()
					.forEach((key, value) -> {
						if (value != null) {
							fakeMDC.put(key, value.toString());
						} else {
							fakeMDC.put(key, nullValueFallback());
						}
					});
		}

		@Override
		public void removeFromMDC(MDCValues mdcValues) {
			mdcValues.values()
					.forEach((key, value) -> fakeMDC.remove(key));
		}
	}

	private final FakeMDCDAO fakeMDCDAO = new FakeMDCDAO();

	@BeforeEach
	void beforeEach() {
		fakeMDCDAO.fakeMDC.put("some-other-key", "some-other-value");
	}

	@Nested
	class With_a_valid_principal {
		CommonMDCFieldRequestFilterHelper requestHelper;
		CommonMDCFieldResponseFilterHelper responseHelper;

		@BeforeEach
		void beforeEach() {
			mockRequestContextPropertiesForRequest();

			requestHelper = CommonMDCFieldRequestFilterHelper.usingDefaults()
					.withMDCDAO(fakeMDCDAO);
			responseHelper = CommonMDCFieldResponseFilterHelper.usingDefaults()
					.withMDCDAO(fakeMDCDAO);

			requestHelper.filter(
					requestCtx,
					new LoggingApp("The App", "The Module", new Semver("1.2.3-SNAPSHOT"), "The Instance"),
					new LoggingSource("/foo/bar", "POST"),
					new LoggingPrincipal("The Principal"),
					new LoggingTenant("The Tenant")
			);
		}

		static final Map<CommonMDCField, Object> EXPECTED_MDC_VALUES = Map.of(
				CommonMDCField.Principal, "The Principal",
				CommonMDCField.RequestSource, "/foo/bar",
				CommonMDCField.RequestSourceArgs, "POST",
				CommonMDCField.TenantId, "The Tenant",
				CommonMDCField.AppProject, "The App",
				CommonMDCField.AppModule, "The Module",
				CommonMDCField.AppVersion, "1.2.3-SNAPSHOT",
				CommonMDCField.AppInstance, "The Instance"
		);

		@ParameterizedTest
		@EnumSource(CommonMDCField.class)
		void mdc_contains_all_standard_fields(CommonMDCField field) {
			// this is implemented via lookup+requireNonNull so we don't accidentally forget to test newly added fields
			var expectedValue = Objects.requireNonNull(EXPECTED_MDC_VALUES.get(field));

			assertThat(fakeMDCDAO.fakeMDC.get(field.name()))
					.isEqualTo(expectedValue);
		}

		@Test
		void RequestPath_contains_only_the_path_of_the_request_URL() {
			assertThat(fakeMDCDAO.fakeMDC.get(CommonMDCField.RequestSource.name()))
					.isEqualTo("/foo/bar");
		}

		@Test
		void response_filter_retains_only_initial_MDC_fields() {
			mockRequestContextPropertiesForResponse();

			responseHelper.filter(requestCtx, Mockito.mock(ContainerResponseContext.class));

			assertThat(fakeMDCDAO.fakeMDC)
					.containsExactly(Map.entry("some-other-key", "some-other-value"));
		}
	}

	@Nested
	class With_a_null_principal {
		CommonMDCFieldRequestFilterHelper sut;

		@BeforeEach
		void beforeEach() {
			mockRequestContextPropertiesForRequest();

			sut = CommonMDCFieldRequestFilterHelper.usingDefaults()
					.withMDCDAO(fakeMDCDAO);

			sut.filter(
					requestCtx,
					new LoggingApp("ignored", "ignored", new Semver("0.0.0"), "ignored"),
					new LoggingSource("ignored", "ignored"),
					null,
					new LoggingTenant("ignored")
			);
		}

		@Test
		void mdc_contains_none_for_the_principal_entry() {
			assertThat(fakeMDCDAO.fakeMDC.get(CommonMDCField.Principal.name()))
					.isEqualTo("");
		}
	}

	@Nested
	class With_a_null_tenant {
		CommonMDCFieldRequestFilterHelper sut;

		@BeforeEach
		void beforeEach() {
			mockRequestContextPropertiesForRequest();

			sut = CommonMDCFieldRequestFilterHelper.usingDefaults()
					.withMDCDAO(fakeMDCDAO);

			sut.filter(
					requestCtx,
					new LoggingApp("ignored", "ignored", new Semver("0.0.0"), "ignored"),
					new LoggingSource("ignored", "ignored"),
					new LoggingPrincipal("The Principal"),
					null
			);
		}

		@Test
		void mdc_contains_none_for_the_principal_entry() {
			assertThat(fakeMDCDAO.fakeMDC.get(CommonMDCField.TenantId.name()))
					.isEqualTo("");
		}
	}

	private void mockRequestContextPropertiesForRequest() {
		Mockito.doAnswer(invocation -> requestContextProperties.put(
						invocation.getArgument(0),
						invocation.getArgument(1)))
				.when(requestCtx).setProperty(ArgumentMatchers.anyString(), ArgumentMatchers.any());
	}

	void mockRequestContextPropertiesForResponse() {
		//noinspection SuspiciousMethodCalls
		Mockito.doAnswer(invocation -> requestContextProperties.get(invocation.getArgument(0)))
				.when(requestCtx).getProperty(ArgumentMatchers.anyString());

	}
}
