# 🖼️ Spring Boot S3 Image CRUD API ( using LocalStack)

This project demonstrates how to build a full **image storage CRUD API** for using
AWS S3 SDK locally using:
- ✅ Java 21 + Spring Boot 3
- ✅ AWS SDK
- ✅ AWS S3 via LocalStack
- ✅ File upload/download
- ✅ Swagger UI for testing
- ✅ UUID key generation to avoid filename collisions

---

## 🚀 Features

- Upload image files via `POST /images/upload`
- Retrieve raw image bytes or redirect to S3 URL
- List all stored images (key + URL)
- Replace/update an image by key
- Delete an image by key
- Works entirely **locally using LocalStack** — no AWS account needed

---

## 🧰 Prerequisites

Make sure you have the following installed:

| Tool            | Minimum Version |
|-----------------|-----------------|
| Docker          | 20.x+           |
| Docker Compose  | v2+             |
| AWS CLI         | 2.x+            |
| Java            | 21              |
| Maven           | 3.8+            |

---

## ⚙️ Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/springboot-s3-demo.git
cd springboot-s3-demo
```

### 2. Start LocalStack & Create Bucket
(see the file)

```bash
chmod +x start-localstack.sh
./start-localstack.sh
```
This will:
- Start LocalStack using Docker Compose
- Wait for it to be ready
- Create the S3 bucket `my-images`

### 3. Run the Spring Boot App

```bash
./mvnw spring-boot:run
```
Or run it from IntelliJ using the `Spingands3demoApplication.java` main class.

---
## 📬 API Usage

Visit Swagger UI:

```bash
http://localhost:8080/swagger-ui.html
```

| Method | Path               | Description                |
|--------|--------------------|----------------------------|
| POST   | /images/upload     | 	Upload an image file      |
| GET    | /images            | List all image keys + URLs |
| GET    | /images/{key}      | 	Get image as byte stream  |
| GET    | 	/images/{key}/url | 	Redirect to S3 image URL  |
| DELETE | /images/{key}      | 	Delete an image           |
| PUT    | 	/images/{key}     | 	Replace/update an image   |



---

## 🧪 Running Tests

```bash
./mvnw test
```

Tests include:
- Unit tests for `ImageService`
- S3 mocking via Mockito
---

## 🧼 Cleanup

```bash
docker-compose down -v
```