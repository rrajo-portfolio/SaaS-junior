package com.fiscalsaas.backend.einvoice;

final class XmlEscaper {

	private XmlEscaper() {
	}

	static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}
}
