package ch.dvbern.oss.commons.logging.mdc;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Slf4j
class MDCDAODefaultImplTest {
	private final MDCDAODefaultImpl sut = new MDCDAODefaultImpl();

	private Map<String, String> mdcBackup;

	@BeforeEach
	void beforeEach() {
		mdcBackup = MDC.getCopyOfContextMap();
		MDC.clear();

		MDC.put("some-other-key", "some-other-value");

		// sanity check
		assertThat(MDC.getCopyOfContextMap())
				.containsExactlyEntriesOf(Map.of("some-other-key", "some-other-value"));
	}

	@AfterEach
	void afterEach() {
		MDC.setContextMap(mdcBackup);
	}

	@Nested
	class applyToMDC {

		@Test
		void applies_all_given_values_to_the_MDC() {
			sut.applyToMDC(MDCValues.of(Map.of("foo", "Foo", "bar", "Bar")));

			assertThat(MDC.getCopyOfContextMap())
					.containsAllEntriesOf(Map.of(
							"foo", "Foo",
							"bar", "Bar"
					));
		}

		@Test
		void retains_existing_values() {
			sut.applyToMDC(MDCValues.of(Map.of("foo", "Foo", "bar", "Bar")));

			assertThat(MDC.getCopyOfContextMap())
					.containsAllEntriesOf(Map.of(
							"some-other-key", "some-other-value"
					));
		}

		@Test
		void applying_a_null_value_puts_a_placeholder_into_the_MDC() {
			Map<String, Object> foo = new HashMap<>();
			foo.put("foo", null);
			sut.applyToMDC(MDCValues.of(foo));

			assertThat(MDC.getCopyOfContextMap())
					.containsEntry("foo", "<null>");
		}
	}

	@Nested
	class removeFromMDC {
		private final MDCValues mdcValues = MDCValues.of(Map.of("foo", "Foo", "bar", "Bar"));

		@BeforeEach
		void beforeEach() {
			sut.applyToMDC(mdcValues);

			sut.removeFromMDC(mdcValues);
		}

		@Test
		void removes_our_values_from_the_MDC() {
			assertThat(MDC.getCopyOfContextMap())
					.doesNotContainKeys("foo", "bar");
		}

		@Test
		void retains_original_values() {
			assertThat(MDC.getCopyOfContextMap())
					.containsAllEntriesOf(Map.of(
							"some-other-key", "some-other-value"
					));
		}
	}

}