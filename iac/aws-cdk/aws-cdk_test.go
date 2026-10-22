package main

import (
	"testing"

	"github.com/aws/aws-cdk-go/awscdk/v2"
	"github.com/aws/aws-cdk-go/awscdk/v2/assertions"
	"github.com/aws/jsii-runtime-go"
)

func TestAwsCdkStack(t *testing.T) {
	defer jsii.Close()
	app := awscdk.NewApp(nil)
	stack := NewAwsCdkStack(app, "MyStack", &AwsCdkStackProps{
		awscdk.StackProps{Env: env()},
	})
	template := assertions.Template_FromStack(stack, nil)
	template.ResourceCountIs(jsii.String("AWS::S3::Bucket"), jsii.Number(1))
	template.ResourceCountIs(jsii.String("AWS::SQS::Queue"), jsii.Number(0))
	template.ResourceCountIs(jsii.String("AWS::SNS::Topic"), jsii.Number(0))
	template.HasResourceProperties(jsii.String("AWS::S3::Bucket"), map[string]interface{}{
		"BucketName": map[string]interface{}{
			"Fn::Join": []interface{}{"", []interface{}{
				"go-cdk-example-",
				map[string]interface{}{"Fn::Select": []interface{}{0, map[string]interface{}{
					"Fn::Split": []interface{}{"-", map[string]interface{}{"Fn::Select": []interface{}{2, map[string]interface{}{
						"Fn::Split": []interface{}{"/", map[string]interface{}{"Ref": "AWS::StackId"}},
					}}}},
				}}},
			}},
		},
		"Tags": []interface{}{map[string]interface{}{"Key": "ManagedBy", "Value": "go-cdk"}},
	})
	template.HasOutput(jsii.String("bucket_name"), map[string]interface{}{
		"Value": map[string]interface{}{"Ref": "DemoBucket"},
	})
	if *stack.Region() != "ap-southeast-1" {
		t.Fatalf("unexpected region: %s", *stack.Region())
	}
}
