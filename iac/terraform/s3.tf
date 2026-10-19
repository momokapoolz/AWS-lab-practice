terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }
}

provider "aws" {
  region = "ap-southeast-1"
}

resource "random_id" "suffix" {
  byte_length = 4 # sinh ra 8 ký tự hex, ví dụ: a1b2c3d4
}

resource "aws_s3_bucket" "demo" {
  bucket = "terraform-demo-${random_id.suffix.hex}"

  tags = {
    ManagedBy = "terraform"
  }
}

output "bucket_name" {
  value = aws_s3_bucket.demo.id
}