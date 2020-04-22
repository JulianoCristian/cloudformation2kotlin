package com.typedpath.awscloudformation.test.withoutextension

import com.typedpath.awscloudformation.*
import com.typedpath.awscloudformation.schema.AWS_S3_Bucket
import com.typedpath.awscloudformation.schema.AWS_S3_BucketPolicy

import com.typedpath.iam2kotlin.IamPolicy
import com.typedpath.iam2kotlin.resources.s3.S3Action

class S3BucketTest {

  fun s3Bucket() {

    val bucket = s3HostingBucket()

    val template =
      CloudFormationTemplate {
        resource("bucket1", bucket)
        resource("bucketPolicy", s3HostingBucketPolicy(this, bucket))
      }
    println(toYaml(template))
  }

  fun s3HostingBucketPolicy(host: CloudFormationTemplate, s3Bucket: AWS_S3_Bucket) : AWS_S3_BucketPolicy {
    val policyDocument = IamPolicy {
//TODO - are these workable
//        Id="MyPolicy"
//        Sid = "PublicReadForGetBucketObjects"
      statement {
        effect = IamPolicy.EffectType.Allow
        principal = mutableMapOf(
          Pair(IamPolicy.PrincipalType.AWS, listOf("*"))
        )
        action( S3Action.GetObject)
        resource ( IamPolicy.Resource(host.join ( "", listOf("arn:aws:s3:::", host.ref(s3Bucket), "/*"))))
      }
    }
    return AWS_S3_BucketPolicy(host.ref(s3Bucket), policyDocument) {
    }
  }
  fun s3HostingBucket() : AWS_S3_Bucket {
    return AWS_S3_Bucket {
      //TODO inject enumeration
      accessControl = "PublicRead"
      websiteConfiguration = AWS_S3_Bucket.WebsiteConfiguration {
        indexDocument = "index.html"
        errorDocument = "error.html"
      }
//  TODO thus property is not in the S3 spec json !?!?!?!          deletionPolicy = "Retain"

    }
  }

  fun s3HostingBucketTest() {
    val s3Bucket = s3HostingBucket()
    val template = CloudFormationTemplate {
      resource("s3HostingBucket", s3Bucket)
      resource ("s3BucketPolicy", s3HostingBucketPolicy(this, s3Bucket))
    }
    //println(template.asYaml())
    println(toYaml(template))
  }
}

fun main (args: Array<String>) {
  S3BucketTest().s3HostingBucketTest()
}
