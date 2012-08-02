package org.grails.plugin.handlebars

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.grails.plugin.resource.mapper.MapperPhase
import org.grails.plugin.resource.ResourceMeta

/**
 * @author Matt Sheehan
 *
 * Precompile .handlebars template files into .js files.
 */
class HandlebarsResourceMapper implements GrailsApplicationAware {

    GrailsApplication grailsApplication

    def phase = MapperPhase.GENERATION

    static defaultIncludes = ['**/*.handlebars']

    def map(ResourceMeta resource, config) {

        def precompiler = getPreCompiler(config)
        File originalFile = resource.processedFile
        File input = getOriginalFileSystemFile(resource.sourceUrl)

        String templateName = calculateTemplateName(resource, config)

        if (resource.sourceUrl) {
            File target = new File(generateCompiledFileFromOriginal(originalFile.absolutePath))

            log.debug "Compiling handlebars file [${originalFile}] into [${target}]"

            try {
                precompiler.precompile input, target, templateName

                resource.processedFile = target
                resource.sourceUrlExtension = 'js'
                resource.contentType = 'text/javascript'
                resource.updateActualUrlFromProcessedFile()
            } catch (e) {
                log.error "error precompiling handlebars file: ${originalFile}", e
            }
        }
    }

	private getPreCompiler(config) {
		if (NodeJSHandlebarsPrecompiler.isAvailable())
			new NodeJSHandlebarsPrecompiler(
					compress: getString(config, 'compress', true),
					helpers: getList(config, 'helpers', [])
			)
		else
			new Precompiler()
	}

    String calculateTemplateName(ResourceMeta resource, config) {
        String pathSeparator = getString(config, 'templatesPathSeparator', '/')
        String root = getString(config, 'templatesRoot')

        String templateName = resource.sourceUrl
        if (root) {
            if (!root.startsWith('/')) {
                root = '/' + root
            }
            if (!root.endsWith('/')) {
                root += '/'
            }
            if (templateName.startsWith(root)) {
                templateName -= root
            }
        }
        templateName = templateName.replaceAll(/(?i)\.handlebars$/, '')
        templateName.split('/').findAll().join(pathSeparator)
    }

    private getString(config, key, defaultVal = null) {
        config[key] instanceof String ? config[key] : defaultVal
    }

	private getList(config, key, defaultVal = null) {
		if (config[key] instanceof String) {
			config[key].split(',')*.trim()
		} else if (config[key] instanceof List) {
			config[key]
		} else {
			defaultVal
		}
	}

    private String generateCompiledFileFromOriginal(String original) {
        original.replaceAll(/(?i)\.handlebars$/, '_handlebars.js')
    }

    private File getOriginalFileSystemFile(String sourcePath) {
        grailsApplication.parentContext.getResource(sourcePath).file
    }
}
