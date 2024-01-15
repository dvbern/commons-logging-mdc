package ch.dvbern.oss.commons.logging.mdc;

import org.slf4j.MDC;

public class MDCDAODefaultImpl implements MDCDAO {

	@Override
	public void applyToMDC(MDCValues mdcValues) {
		mdcValues.values()
				.forEach((key, value) -> {
					// MDC.put only supports null values *if the underlying implementation supports it*.
					if (value != null) {
						MDC.put(key, value.toString());
					} else {
						MDC.put(key, nullValueFallback());
					}
				});
	}

	@Override
	public void removeFromMDC(MDCValues mdcValues) {
		mdcValues.values().keySet()
				.forEach(MDC::remove);
	}

}
