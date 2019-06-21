package com.typedpath.awscloudformation

import java.util.*

enum class ParameterType(val awsTypeName: String) {
  STRING("String"),
  AWS_EC2_KeyPair("AWS::EC2::KeyPair::KeyName");

  override fun toString(): String {
    return awsTypeName
  }
}

open class CloudFormationTemplate {
  val AWSTemplateFormatVersion = "2010-09-09"

  class Parameter(val type: ParameterType, val description: String) {
    var default: String? = null
    var minLength: Int? = null
    var maxLength: Int? = null
    var allowedPattern: String? = null
  }

  //TO make this lower case
  var parameters = mutableMapOf<String, Parameter>()

  fun parameter(name: String, parameter: Parameter) {
    this.parameters.put(name, parameter)
  }

  // https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/outputs-section-structure.html
  class Output(val value: String) {
    val description: String? = null

    class Export(val name: String)

    var export: Export? = null
  }

  var outputs = mutableMapOf<String, Output>()

  fun output(logicalId: String, output: Output) {
    this.outputs.put(logicalId, output)
  }

  private val currentStackName = "AWS::StackName"
  private val currentRegionName = "AWS::Region"
  private val currentAccountId = "AWS::AccountId"

  var resources = mutableMapOf<String, Resource>()

  fun resource(name: String, resource: Resource) {
    resources.put(name, resource)
  }


  private val refs = mutableMapOf<String, Any>()

  fun ref(resource: Any): String {
    val id = UUID.randomUUID().toString()
    refs.put(id, resource)
    return id
  }

  class StaticResource(val name: String) : Resource() {
    override fun getResourceType(): String = name
  }

  private fun refStatic(name: String): String {
    val id = UUID.randomUUID().toString()
    refs.put(id, StaticResource(name))
    return id
  }

  fun refCurrentStack(): String = refStatic(currentStackName)

  fun refCurrentRegion(): String = refStatic(currentRegionName)

  fun refCurrentAccountId(): String = refStatic(currentAccountId)


  class JoinStatement(val delimiter: String, val items: List<String>) {

    val parameters: List<Any>

    init {
      parameters = listOf(delimiter, items)
    }

    fun valuePair() = mapOf(Pair("!Join", parameters))

  }

  private val intrinsicFunctionParameters = mutableSetOf<Any>()

  fun join(delimiter: String, items: List<String>): String {
    val id = UUID.randomUUID().toString()
    // could be Value: !GetAtt [S3Bucket, WebsiteURL]
    val functionCall = JoinStatement(delimiter, items)
    refs.put(id, functionCall.valuePair())
    intrinsicFunctionParameters.add(functionCall.parameters)
    return id
  }

  fun Parameter(
    type: ParameterType,
    description: String,
    init: CloudFormationTemplate.Parameter.() -> Unit
  ): CloudFormationTemplate.Parameter =
    CloudFormationTemplate.Parameter(type, description).apply(init)


  open class TemplateReference(val name: String) {
    open fun materialise(): String {
      return "!Ref $name"
    }
  }

  class AttributeReference(val resourceName: String, val attributeName: String) {
    fun materialise(): Map<String, List<String>> {
      return mapOf(Pair("Fn::GetAtt", listOf(resourceName, attributeName)))
    }
  }

  fun deref(value: Any): Any {
    val obj = refs.get(value)
    if (obj == null) throw RuntimeException("cant deref $obj")
    return if (obj is StaticResource) {
      TemplateReference(obj.name)
    } else if (obj is Resource) {
      val entries = resources.filter { entry -> entry.value == obj }.map { entry2 -> entry2.key }
      val resourceName =
        if (entries.size == 0) obj.getResourceType()
        else entries.get(0)
      TemplateReference(resourceName)
    } else if (obj is Parameter) {
      val names = parameters.filter { entry -> entry.value == obj }.map { entry2 -> entry2.key }
      if (names.size == 0) throw java.lang.RuntimeException("cant reference unkown paramete ${obj}")
      TemplateReference(names.get(0))
    } else if (obj is Resource.Attribute) {
      val entries = resources.filter { entry -> entry.value == obj.parent }.map { entry2 -> entry2.key }

      if (entries.size == 0) {
        throw java.lang.RuntimeException("failed to locate resource ${obj.parent} for attribute ${obj.name}")
      }
      AttributeReference(entries.get(0), obj.name)
    } else
      obj
  }

  fun isRef(value: Any) = refs.containsKey(value)

}

fun cloudFormationTemplate(init: CloudFormationTemplate.() -> Unit): CloudFormationTemplate =
  CloudFormationTemplate().apply(init)

