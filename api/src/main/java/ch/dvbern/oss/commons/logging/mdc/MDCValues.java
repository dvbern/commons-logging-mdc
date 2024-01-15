package ch.dvbern.oss.commons.logging.mdc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Value;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
// mimic record behavior
@Accessors(fluent = true)
public final class MDCValues {
	public static final String EMPTY_VALUE = "";

	private final Map<String, @Nullable Object> values;

	private MDCValues(Map<String, @Nullable Object> values) {
		this.values = Collections.unmodifiableMap(values);
	}

	public static MDCValues empty() {
		return new MDCValues(Map.of());
	}

	private static Map<String, Object> copyOf(Map<String, @Nullable Object> values) {
		// cannot use Map.copyOf because it does not allow null values,
		// but we want our lib to be as forgiving as possible!
		return new HashMap<>(values);
	}

	public static MDCValues of(Map<String, @Nullable Object> entries) {
		return new MDCValues(copyOf(entries));
	}

	public static MDCValues fromStandardFields(
			LoggingApp app,
			LoggingSource source,
			@Nullable LoggingPrincipal principal,
			@Nullable LoggingTenant tenant
	) {
		return empty()
				.withStandardFields(app, source, principal, tenant);
	}

	public MDCValues with(String key, Object value) {
		var copy = copyOf(values);
		copy.putAll(values);
		copy.put(key, value);

		return new MDCValues(copy);
	}

	public MDCValues withStandardFields(
			LoggingApp app,
			LoggingSource source,
			@Nullable LoggingPrincipal principal,
			@Nullable LoggingTenant tenant
	) {
		var principalDisplayed = Optional.ofNullable(principal)
				.map(LoggingPrincipal::id)
				.orElse(EMPTY_VALUE);
		var tenantDisplayed = Optional.ofNullable(tenant)
				.map(LoggingTenant::id)
				.orElse(EMPTY_VALUE);

		var entries = Map.<String, Object>of(
				CommonMDCField.Principal.name(), principalDisplayed,
				CommonMDCField.RequestSource.name(), source.value(),
				CommonMDCField.RequestSourceArgs.name(), source.args(),
				CommonMDCField.TenantId.name(), tenantDisplayed,
				CommonMDCField.AppName.name(), app.appName(),
				CommonMDCField.AppModule.name(), app.appModule(),
				CommonMDCField.AppVersion.name(), app.appVersion().toString(),
				CommonMDCField.AppInstance.name(), app.appInstance()
		);

		return of(entries);
	}

}
