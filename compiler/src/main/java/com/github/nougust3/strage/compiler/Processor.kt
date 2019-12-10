package com.github.nougust3.strage.compiler

import com.github.nougust3.strage.annotation.Adapter
import com.github.nougust3.strage.annotation.BindId
import com.github.nougust3.strage.annotation.BindName
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import me.eugeniomarletti.kotlin.metadata.jvm.descriptor
import me.eugeniomarletti.kotlin.metadata.jvm.internalName
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.tools.Diagnostic

data class Binder(
    val method: String,
    val type: String,
    val layout: Int,
    val viewTypes: List<ViewType>,
    val layoutName: String = ""
) {
    constructor(method: String, type: String, layout: String, viewTypes: List<ViewType>):
            this(method, type, -1, viewTypes, layout)
}

data class ViewType(val name: String, val type: String)

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
class Processor: KotlinAbstractProcessor() {

    companion object {
        const val KOTLIN_DIRECTORY_NAME = "kapt.kotlin.generated"
    }

    private val annotation = Adapter::class.java

    private val bindIdAnnotation = BindId::class.java
    private val bindNameAnnotation = BindName::class.java

    private fun getBinders(roundEnv: RoundEnvironment, element: Element): List<Binder> {
        val result = ArrayList<Binder>()

        result.addAll(roundEnv
            .getElementsAnnotatedWith(bindIdAnnotation)
            .filter {
                it.enclosingElement == element
            }
            .map {
                val params = (it.asType() as ExecutableType).parameterTypes
                val dataType = if (params.first().toString() == "java.lang.String") "kotlin.String" else params.first().toString()
                val viewTypes = (it as ExecutableElement).parameters.takeLast(params.size - 1).map {
                    ViewType(it.internalName, it.asType().toString())
                }
                Binder(it.internalName, dataType, it.getAnnotation(bindIdAnnotation).id, viewTypes)
            })

        result.addAll(roundEnv
            .getElementsAnnotatedWith(bindNameAnnotation)
            .filter {
                it.enclosingElement == element
            }
            .map {
                val params = (it.asType() as ExecutableType).parameterTypes
                val dataType = if (params.first().toString() == "java.lang.String") "kotlin.String" else params.first().toString()
                val viewTypes = (it as ExecutableElement).parameters.takeLast(params.size - 1).map {
                    ViewType(it.internalName, it.asType().toString())
                }
                var layout = it.internalName
                repeat(layout.count { it.isUpperCase() }) {
                    val index = layout.indexOfFirst { it.isUpperCase() }
                    layout = layout.substring(0, index) + "_" + layout[index].toLowerCase() + layout.substring(index + 1, layout.length)
                }
                Binder(it.internalName, dataType, layout, viewTypes)
            })

        return result
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(annotation)
            .forEach {
                if (it.kind != ElementKind.CLASS) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Can annotate only classes")
                    return@forEach
                }

                val qualifiedName = elementUtils.getPackageOf(it).qualifiedName.toString()
                val packageName = elementUtils.getPackageOf(it).toString()
                val elementName = it.simpleName.toString()
                val binders = getBinders(roundEnv, it)
                startClassGeneration(qualifiedName, elementName, packageName, binders)
            }
        return false
    }

    private fun startClassGeneration(pack: String, name: String, packg: String, methods: List<Binder>) {
        adapterName = name
        adapterPackage = packg

        val fileName = "${name}Adapter"
        val classBuilder = TypeSpec.classBuilder(fileName)

        classBuilder.addProperty(adapterFieldName, ClassName(adapterPackage, adapterName), KModifier.PRIVATE, KModifier.FINAL)
        classBuilder.addInitializerBlock(CodeBlock.builder().add("$adapterFieldName = $adapterName()\n").build())
        classBuilder.addFunction(getMethod(methods))

        FileSpec.builder(pack, fileName)
            .addType(classBuilder.build())
            .build()
            .writeTo(File(options[KOTLIN_DIRECTORY_NAME], "$fileName.kt"))
    }

    private val adapterFieldName = "adapter"
    private val contextClass = ClassName("android.content", "Context")
    private val adapterClass = ClassName("com.githab.nougust3.strage", "StrageAdapter")
    private var adapterName = ""
    private var adapterPackage = ""

    private fun getMethod(methods: List<Binder>) =
        FunSpec.builder("get")
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
            .addParameter("context", contextClass)
            .addCode("return \n\tStrage(context)\n" + methods.joinToString("") {
                "\t\t.bind<${it.type}>(${if (it.layout != -1) it.layout else "R.layout.${it.layoutName}" }) {\n" +
                "\t\t\t$adapterFieldName.${it.method}(\n" +
                "\t\t\t\tit" + (if (it.viewTypes.isNotEmpty()) ",\n" else "\n") +
                it.viewTypes.joinToString(",\n") {
                    "\t\t\t\tthis.view<${it.type}>(R.id.${it.name})"
                } +
                "\n\t\t\t)\n" +
                "\t\t}\n"
            })
            .returns(adapterClass)
            .build()

    private fun getBinderCode(methods: List<Binder>): String {
        fun getLayoutCode(binder: Binder) =
            if (binder.layout == -1) "R.layout.${binder.layoutName}"
            else binder.layout.toString()

        val builder = StringBuilder()

        

        return builder.toString()
    }


    override fun getSupportedAnnotationTypes(): Set<String> = setOf(annotation.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

}