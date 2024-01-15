package ch.dvbern.oss.commons.logging.mdc;

import java.util.Map;

import com.vdurmont.semver4j.Semver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("EmptyClass")
class MDCValuesTest {

	@Nested
	class empty {

		@Test
		void creates_MDCValues_with_no_values() {
			MDCValues actual = MDCValues.empty();

			assertThat(actual.values())
					.isEmpty();
		}
	}

	@Nested
	class of {
		@Test
		void creates_MDCValues_with_only_the_given_entries() {
			Map<String, Object> values = Map.of(
					"key1", "value1",
					"key2", "value2");

			MDCValues mdcValues = MDCValues.of(values);

			assertThat(mdcValues.values())
					.containsExactlyInAnyOrderEntriesOf(values);
		}
	}

	@Nested
	class fromStandardFields {
		private final LoggingApp app = new LoggingApp("The App", new Semver("1.2.3-SNAPSHOT"), "The Instance");
		private final LoggingSource source = new LoggingSource("/foo/bar", "POST");
		private final LoggingPrincipal principal = new LoggingPrincipal("The Principal");
		private final LoggingTenant tenant = new LoggingTenant("The Tenant");

		@Test
		void creates_MDCValues_with_all_standard_fields_filled() {

			MDCValues mdcValues = MDCValues.fromStandardFields(app, source, principal, tenant);

			assertThat(mdcValues.values())
					.containsExactlyInAnyOrderEntriesOf(Map.of(
							CommonMDCField.AppName.name(), "The App",
							CommonMDCField.AppVersion.name(), "1.2.3-SNAPSHOT",
							CommonMDCField.AppInstance.name(), "The Instance",
							CommonMDCField.RequestSource.name(), "/foo/bar",
							CommonMDCField.RequestSourceArgs.name(), "POST",
							CommonMDCField.Principal.name(), "The Principal",
							CommonMDCField.TenantId.name(), "The Tenant"
					));
		}

		@Test
		void sets_principal_to_empty_string_if_missing() {
			MDCValues mdcValues = MDCValues.fromStandardFields(app, source, null, tenant);

			assertThat(mdcValues.values())
					.containsEntry(CommonMDCField.Principal.name(), "");
		}

		@Test
		void sets_tenantId_to_empty_string_if_missing() {
			MDCValues mdcValues = MDCValues.fromStandardFields(app, source, principal, null);

			assertThat(mdcValues.values())
					.containsEntry(CommonMDCField.TenantId.name(), "");
		}
	}

	@Nested
	class with {
		MDCValues empty;
		MDCValues withFoo;
		MDCValues withFooBar;

		@BeforeEach
		void beforeEach() {
			empty = MDCValues.empty();

			withFoo = empty.with("foo", "Foo");
			withFooBar = withFoo.with("bar", "Bar");
		}

		@Test
		void appends_the_given_entries() {
			assertThat(withFoo.values())
					.containsExactlyInAnyOrderEntriesOf(Map.of("foo", "Foo"));
			assertThat(withFooBar.values())
					.containsExactlyInAnyOrderEntriesOf(Map.of("foo", "Foo", "bar", "Bar"));
		}

		@Test
		void creates_a_new_instance_for_each_call() {

			assertThat(empty)
					.isNotSameAs(withFoo)
					.isNotSameAs(withFooBar);

			assertThat(withFoo)
					.isNotSameAs(withFooBar);
		}
	}

}