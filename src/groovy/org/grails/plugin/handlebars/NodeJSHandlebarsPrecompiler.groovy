package org.grails.plugin.handlebars

import org.lesscss.LessException

class NodeJSHandlebarsPrecompiler {

	boolean compress = true
	List<String> helpers = []

	static isAvailable() {
		isOperatingSystemSupported() && getNodePath() && getHandlebarsPath()
	}

	static isOperatingSystemSupported() {
		switch (System.getProperty("os.name").toLowerCase()) {
			case "linux": true; break
			default: false
		}
	}

	static getHandlebarsPath() {
		getPathTo "handlebars"
	}

	static getNodePath() {
		getPathTo "node"
	}

	static getPathTo(program) {
		def p = "/usr/bin/which $program".execute()
		p.waitFor()
		def path = p.text.trim()
		new File(path).exists() ? path : null
	}

	def precompile(input, target, templateName) {
		def args = createArgs(input, target, templateName)
		def (process, output) = execute(args)
		if (process.exitValue()) {
			throw new NodeJSHandlebarsException(output.toString())
		}
	}

	private createArgs(input, target, templateName) {
		def node = getNodePath()
		def handlebars = getHandlebarsPath()
		def root = input.absolutePath - "/${templateName}" - ".handlebars" - ".hbs" - ".mustache"
		def args = [node, handlebars, input.absolutePath, "-r", root, "-f", target.absolutePath]
		if (compress) args << "-m"
		args + helpers.collect { ["-k", it] }.flatten()
	}

	private execute(args) {
		def p = args.execute()
		def sb = new StringBuilder()
		p.consumeProcessOutput(sb, sb)
		p.waitFor()
		[p, sb]
	}
}