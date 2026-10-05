package github.businessdirt.eurybium.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EurybiumModule

class ModuleProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModuleProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}

class ModuleProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private var eurybiumEvent: KSType? = null
    private var warnings = mutableListOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        eurybiumEvent = resolver.getClassDeclarationByName("github.businessdirt.eurybium.core.events.EurybiumEvent")?.asStarProjectedType()

        val symbols = resolver.getSymbolsWithAnnotation(EurybiumModule::class.qualifiedName!!).toList()
        logger.warn("Found ${symbols.size} symbols with @EurybiumModule")
        val validSymbols = symbols.mapNotNull { validateSymbol(it) }

        if (validSymbols.isNotEmpty()) {
            generateFile(validSymbols)
        }

        return emptyList()
    }

    private fun validateSymbol(symbol: KSAnnotated): KSClassDeclaration? {
        if (!symbol.validate()) {
            logger.warn("Symbol is not valid: $symbol")
            return null
        }

        if (symbol !is KSClassDeclaration) {
            logger.error("@EurybiumModule is only valid on class declarations", symbol)
            return null
        }

        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@EurybiumModule is only valid on kotlin objects", symbol)
            return null
        }

        return symbol
    }

    private fun generateFile(symbols: List<KSClassDeclaration>) {

        if (warnings.isNotEmpty()) {
            warnings.forEach { logger.warn(it) }
            error("${warnings.size} errors related to event annotations found, please fix them before continuing. Click on the kspKotlin build log for more information.")
        }

        val sources = symbols.mapNotNull { it.containingFile }.toTypedArray()
        val dependencies = Dependencies(true, *sources)

        val file = codeGenerator.createNewFile(dependencies, "github.businessdirt.eurybium.core.modules", "LoadedModules")

        OutputStreamWriter(file).use {
            it.write("package github.businessdirt.eurybium.core.modules\n\n")
            it.write("@Suppress(\"LargeClass\")\n")
            it.write("object LoadedModules {\n")
            it.write("    val modules: List<Any> = buildList {\n")

            symbols.forEach { symbol ->
                it.write("        add(${symbol.qualifiedName!!.asString()})\n")
            }

            it.write("    }\n")
            it.write("}\n")
        }

        logger.warn("Generated LoadedModules file with ${symbols.size} modules")
    }
}
