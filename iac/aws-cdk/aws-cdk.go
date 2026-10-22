package main

import (
	"github.com/aws/aws-cdk-go/awscdk/v2"
	"github.com/aws/aws-cdk-go/awscdk/v2/awss3"
	"github.com/aws/constructs-go/constructs/v10"
	"github.com/aws/jsii-runtime-go"
)

type AwsCdkStackProps struct {
	awscdk.StackProps
}

func NewAwsCdkStack(scope constructs.Construct, id string, props *AwsCdkStackProps) awscdk.Stack {
	var sprops awscdk.StackProps
	if props != nil {
		sprops = props.StackProps
	}
	stack := awscdk.NewStack(scope, &id, &sprops)

	// The stack UUID provides a stable eight-character hex suffix across deployments.
	stackUUID := awscdk.Fn_Select(jsii.Number(2), awscdk.Fn_Split(jsii.String("/"), stack.StackId()))
	suffix := awscdk.Fn_Select(jsii.Number(0), awscdk.Fn_Split(jsii.String("-"), stackUUID))
	bucket := awss3.NewCfnBucket(stack, jsii.String("DemoBucket"), &awss3.CfnBucketProps{
		BucketName: awscdk.Fn_Join(jsii.String(""), &[]*string{jsii.String("go-cdk-example-"), suffix}),
		Tags:       &[]*awscdk.CfnTag{{Key: jsii.String("ManagedBy"), Value: jsii.String("go-cdk")}},
	})

	awscdk.NewCfnOutput(stack, jsii.String("bucket_name"), &awscdk.CfnOutputProps{
		Value: bucket.Ref(),
	})

	return stack
}

func main() {
	defer jsii.Close()
	app := awscdk.NewApp(nil)
	NewAwsCdkStack(app, "AwsCdkStack", &AwsCdkStackProps{
		awscdk.StackProps{Env: env()},
	})
	app.Synth(nil)
}

func env() *awscdk.Environment {
	return &awscdk.Environment{Region: jsii.String("ap-southeast-1")}
}
