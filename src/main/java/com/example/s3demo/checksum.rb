require 'aws-sdk-s3'
require 'digest'
require 'base64'

s3 = Aws::S3::Client.new(region: 'ap-southeast-1')

# path = 'example.txt'

# 1. Đọc file và tính SHA-256 (base64 của hash nhị phân)
data     = File.binread(path)
checksum = Base64.strict_encode64(Digest::SHA256.digest(data))

# 2. Upload kèm checksum đã tính sẵn
resp = s3.put_object(
  bucket: 'my-bucket',
  key: 'uploads/example.txt',
  body: data,
  content_type: 'text/plain',
  checksum_sha256: checksum
)

puts "Uploaded. Checksum S3 lưu: #{resp.checksum_sha256}"