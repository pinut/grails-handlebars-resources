package org.grails.plugin.handlebars

class NodeJSHandlebarsException extends RuntimeException {
	NodeJSHandlebarsException() {
	}

	NodeJSHandlebarsException(String s) {
		super(s)
	}

	NodeJSHandlebarsException(String s, Throwable throwable) {
		super(s, throwable)
	}

	NodeJSHandlebarsException(Throwable throwable) {
		super(throwable)
	}
}
