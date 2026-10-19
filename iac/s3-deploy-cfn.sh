#!/usr/bin/env bash
set -euo pipefail
# This script is used to deploy the S3 bucket using CloudFormation
# It will create a new S3 bucket with the specified name and region

STACK_NAME=S3-EXAMPLE-STACK
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

aws cloudformation deploy \
    --template-file "$SCRIPT_DIR/s3.yml" \
    --stack-name "$STACK_NAME" \
    --region ap-southeast-1

echo "Deployment complete. Stack name: $STACK_NAME"
