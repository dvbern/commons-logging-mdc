package ch.dvbern.oss.commons.logging.mdc;

public interface MDCDAO {
	default String nullValueFallback() {
		return "<null>";
	}

	void applyToMDC(MDCValues mdcValues);

	void removeFromMDC(MDCValues mdcValues);
}
