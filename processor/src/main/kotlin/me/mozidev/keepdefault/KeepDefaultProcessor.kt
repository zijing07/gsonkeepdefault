package me.mozidev.keepdefault

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.serialization.deserialization.getName
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * created by zijing on 2019/1/15
 */
@AutoService(Processor::class)
class KeepDefaultProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {

    private val annotation = KeepDefault::class.java
    private var fileDir: String? = null
    private var showErrorIfNoDefault: Boolean = false

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        messager.printMessage(Diagnostic.Kind.WARNING, "init")
        fileDir = processingEnv.options["kapt.kotlin.generated"]
    }

    override fun getSupportedAnnotationTypes() = setOf(annotation.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        for (type in roundEnv.getElementsAnnotatedWith(annotation)) {

            val keepDefaultAnnotation = type.getAnnotation(annotation)

            showErrorIfNoDefault = keepDefaultAnnotation.showErrorIfNoDefault

            val typeMetadata = type.kotlinMetadata as? KotlinClassMetadata
            if (typeMetadata == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@KeepDefault cannot be applied to $type: must be a Kotlin class")
                continue
            }

            val proto = typeMetadata.data.classProto
            if (!proto.isDataClass) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@KeepDefault cannot be applied to @type: must be a Data class")
                continue
            }

            val typeName = type.asType().asTypeName() as ClassName

            genTypeHelperClassFile(typeName, typeMetadata)
        }

        return true
    }

    private fun genTypeHelperClassFile(typeName: ClassName, kotlinClassMetadata: KotlinClassMetadata): String {

        val helperClassName = "KeepDefaultHelper_${typeName.simpleNames.joinToString("_")}"

        val result = FileSpec.builder(typeName.packageName, helperClassName)
        result.addComment("Code generated by gsonkeepdefault. Do not edit.")

        result.addImport(typeName.packageName, typeName.simpleNames.joinToString("."))

        result.addType(TypeSpec.objectBuilder(helperClassName)
            .addSuperinterface(IKeepDefault::class.asClassName().parameterizedBy(typeName))
            .addFunction(generateTryKeepDefault(typeName, kotlinClassMetadata))
            .build())

        result.build().writeTo(File(fileDir, helperClassName).also { if (!it.exists()) it.mkdirs() })

        return helperClassName
    }

    private fun generateTryKeepDefault(typeName: ClassName, metadata: KotlinClassMetadata): FunSpec {

        val (nameAllocator, classProto) = metadata.data

        // the Gson-parsed data object
        val originParam = ParameterSpec
            .builder("originData", typeName.asNullable())
            .build()

        // retrieve all the fields from DataClass' constructor
        val propList = classProto.constructorList.single { it.isPrimary }.valueParameterList

        val noDefaultMarked = propList.count { it.declaresDefaultValue } == 0
        if (showErrorIfNoDefault && noDefaultMarked) {
            messager.printMessage(Diagnostic.Kind.ERROR, "There is no default value for any property")
        }

        // if non field is marked with "default", return the Gson-pared data object
        val statements = if (noDefaultMarked) {
            generateNoDefaultSpecified()
        } else {
            generateCheckDefault(typeName, nameAllocator, propList)
        }

        return FunSpec.builder("tryKeepDefault")
            .addParameter(originParam)
            .addModifiers(KModifier.OVERRIDE)
            .returns(typeName.asNullable())
            .addStatement("if (originData == null) return originData")
            .apply {
                statements.forEach { addStatement(it) }
            }
            .build()
    }

    private fun generateNoDefaultSpecified(): List<String> {
        return listOf("return originData")
    }

    /**
     * do the real work,
     * 1. check all the fields set with "default" modifier
     * 2. check if it is "nullable"
     * 3. override the null value of "NonNull-Default" field
     */
    private fun generateCheckDefault(
        typeName: TypeName,
        nameAllocator: NameResolver,
        propList: List<ProtoBuf.ValueParameter>
    ): List<String> {
        propList.filter { !it.declaresDefaultValue }
            .map { nameAllocator.getName(it.name) }
            .takeIf { showErrorIfNoDefault && it.isNotEmpty() }
            ?.forEach {
                messager.printMessage(Diagnostic.Kind.ERROR, "There is no default value for $it")
            }

        val preCheckCond = propList.filter { it.declaresDefaultValue }.joinToString(" && ") { p ->
            val n = nameAllocator.getName(p.name)
            "originData.$n != null"
        }
        val preCheckStmt = "if ($preCheckCond) return originData"

        val defaultData = propList.filter { !it.declaresDefaultValue }.joinToString(", \n") {
            val n = nameAllocator.getName(it.name)
            "$n = originData.$n"
        }
        val defaultDataStmt = "val defaultData = $typeName(\n$defaultData)"

        val propWithDefault = propList.joinToString(", \n") { p ->
            val n = nameAllocator.getName(p.name)
            if (!p.type.nullable && p.declaresDefaultValue) {
                "$n = if(null == originData.$n) defaultData.$n else originData.$n"
            } else {
                "$n = originData.$n"
            }
        }
        val correctDataStmt = "return $typeName(\n$propWithDefault)"

        return listOf(preCheckStmt, defaultDataStmt, correctDataStmt)
    }
}
