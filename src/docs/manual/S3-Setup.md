# Java + Amazon S3 demo

Demo Spring Boot với 2 hàm trong `S3Controller`:

- `uploadText`: PUT `/s3?key=demo/hello.txt`, ghi nội dung UTF-8 vào S3.
- `downloadText`: GET `/s3?key=demo/hello.txt`, đọc nội dung UTF-8 từ S3.

## Điều kiện chạy

1. JDK 24 (đúng với `pom.xml`). Không cần cài Maven riêng vì đã có Maven Wrapper.
2. Một S3 bucket đã tồn tại và region đúng của bucket.
3. AWS credentials hợp lệ: profile trong `%USERPROFILE%\.aws\credentials`, hoặc biến môi trường `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` và `AWS_SESSION_TOKEN` nếu dùng credentials tạm thời. Không ghi secret vào source code. SDK dùng [default credentials chain](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html).
4. IAM principal cần `s3:PutObject` và `s3:GetObject` trên `arn:aws:s3:::TEN_BUCKET/demo/*` cho các ví dụ bên dưới. Bucket dùng SSE-KMS có thể cần thêm quyền KMS và key policy phù hợp.

Giữ bucket private, bật Block Public Access và mã hóa mặc định (SSE-S3 hoặc SSE-KMS). SDK kết nối S3 qua HTTPS. Xem [S3 security best practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/security-best-practices.html).

## Chạy trên PowerShell

```powershell
$env:AWS_REGION = 'ap-southeast-1' # đổi theo region thực tế của bucket
$env:S3_BUCKET = 'TEN_BUCKET'
# Nếu đã cấu hình profile riêng:
# $env:AWS_PROFILE = 'ten-profile'
.\mvnw.cmd spring-boot:run
```

Thiếu `S3_BUCKET` sẽ khiến ứng dụng dừng khi khởi động với thông báo cấu hình. Region mặc định là `ap-southeast-1`. Ứng dụng chỉ lắng nghe tại `127.0.0.1:8080` vì demo chưa có xác thực API.

Mở terminal thứ hai để thử:

```powershell
Invoke-RestMethod -Method Put -Uri 'http://127.0.0.1:8080/s3?key=demo/hello.txt' -ContentType 'text/plain; charset=utf-8' -Body 'Hello AWS S3'
Invoke-RestMethod -Method Get -Uri 'http://127.0.0.1:8080/s3?key=demo/hello.txt'
```

Chạy kiểm thử (mock S3, không cần credentials hay bucket thật):

```powershell
.\mvnw.cmd test
```

Demo chỉ dành cho text nhỏ: nội dung được giữ toàn bộ trong RAM. PUT cùng key sẽ ghi đè nội dung hiện tại (bucket có versioning sẽ giữ phiên bản cũ). Lỗi AWS được truyền lên Spring và trả HTTP 500; xem log để biết nguyên nhân. Không tự tạo bucket hoặc tự gọi AWS khi khởi động.

S3 có thể tính phí lưu trữ, request và truyền dữ liệu; xem [S3 pricing](https://aws.amazon.com/s3/pricing/).
