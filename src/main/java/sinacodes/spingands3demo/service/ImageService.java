package sinacodes.spingands3demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sinacodes.spingands3demo.DTO.ImageMetadata;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageService {

   private final S3Client s3Client;

   @Value("${aws.s3.bucket}")
   private String bucket;

   @Value("${aws.s3.bucket}")
   private String defaultBucket;

   @Value("${aws.s3.endpoint}")
   private String endpoint;

   public ImageService(S3Client s3Client) {
      this.s3Client = s3Client;
   }

   @PostConstruct
   public void initBucket() {
      try {
         HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
               .bucket(bucket)
               .build();

         s3Client.headBucket(headBucketRequest);
      } catch (NoSuchBucketException e) {
         System.out.println("Bucket doesn't exist. Creating: " + bucket);
         CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
               .bucket(bucket)
               .build();
         s3Client.createBucket(createBucketRequest);
      } catch (S3Exception e) {
         if (e.statusCode() == 404) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
         } else {
            throw new RuntimeException("Failed to verify/create bucket: " + bucket, e);
         }
      }
   }

   private String resolveBucket(String bucketParam) {
      return (bucketParam != null && !bucketParam.isBlank()) ? bucketParam : defaultBucket;
   }


   public String uploadImage(MultipartFile file, String bucketOverride) {
      String bucket = resolveBucket(bucketOverride);
      String extension = "";

      String originalName = file.getOriginalFilename();
      if (originalName != null && originalName.contains(".")) {
         extension = originalName.substring(originalName.lastIndexOf('.'));
      }

      String key = UUID.randomUUID() + extension;

      PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build();

      try {
         s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      } catch (IOException e) {
         throw new RuntimeException("Failed to upload image", e);
      }

      return key;
   }


   public byte[] getImage(String key, String bucketOverride) {
      String bucket = resolveBucket(bucketOverride);
      GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

      try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest)) {
         return response.readAllBytes();
      } catch (IOException e) {
         throw new RuntimeException("Failed to fetch image", e);
      }
   }

   public void deleteImage(String key, String bucketOverride) {
      String bucket = resolveBucket(bucketOverride);
      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

      s3Client.deleteObject(deleteRequest);
   }

   public List<ImageMetadata> listImages(String bucketOverride) {
      String bucket = resolveBucket(bucketOverride);
      ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
            .bucket(bucket)
            .build();

      ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);

      return response.contents().stream()
            .map(s3Object -> new ImageMetadata(
                  s3Object.key(),
                  getImageUrl(s3Object.key(), bucket)
            ))
            .collect(Collectors.toList());
   }

   public String getImageUrl(String key, String bucketOverride) {
      String bucket = resolveBucket(bucketOverride);
      return String.format("%s/%s/%s", endpoint, bucket, key);
   }

   public ImageMetadata getMetadataFromUrl(String url, String bucketOverride) {
      String bucket = resolveBucket(bucketOverride);
      // Remove prefix like: http://localhost:4566/my-images/
      String prefix = String.format("%s/%s/", endpoint, bucket);

      if (!url.startsWith(prefix)) {
         throw new IllegalArgumentException("URL does not match expected S3 format");
      }

      String key = url.substring(prefix.length());
      return new ImageMetadata(key, url);
   }

   public List<String> listBuckets() {
      ListBucketsResponse response = s3Client.listBuckets();
      return response.buckets().stream()
            .map(Bucket::name)
            .collect(Collectors.toList());
   }

}
