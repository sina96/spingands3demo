package sinacodes.spingands3demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import sinacodes.spingands3demo.DTO.ImageMetadata;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ImageServiceTest {

   private S3Client s3Client;
   private ImageService imageService;

   @BeforeEach
   void setup() {
      s3Client = mock(S3Client.class);
      imageService = new ImageService(s3Client);
      setField("bucket", "my-images");
      setField("defaultBucket", "my-images");
      setField("endpoint", "http://localhost:4566");
   }

   private void setField(String fieldName, String value) {
      try {
         var field = ImageService.class.getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(imageService, value);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Test
   void uploadImageShouldReturnGeneratedKey() throws IOException {
      MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "test-image".getBytes()
      );

      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

      String key = imageService.uploadImage(file, null);

      assertNotNull(key);
      assertTrue(key.endsWith(".jpg"));
   }

   @Test
   void getImageShouldReturnByteArray() throws IOException {
      byte[] imageBytes = "image-bytes".getBytes();
      ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            new ByteArrayInputStream(imageBytes)
      );

      when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);

      byte[] result = imageService.getImage("abc.jpg", null);
      assertArrayEquals(imageBytes, result);
   }

   @Test
   void deleteImageShouldInvokeS3Client() {
      imageService.deleteImage("abc.jpg", null);
      verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
   }

   @Test
   void listImagesShouldReturnImageMetadataList() {
      S3Object object = S3Object.builder()
            .key("image1.jpg")
            .size(1234L)
            .lastModified(Instant.parse("2024-01-01T10:00:00Z"))
            .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(ListObjectsV2Response.builder().contents(object).build());

      List<ImageMetadata> result = imageService.listImages(null);

      assertEquals(1, result.size());
      assertEquals("image1.jpg", result.getFirst().getKey());
      assertEquals(1234L, result.getFirst().getSize());
      assertEquals("http://localhost:4566/my-images/image1.jpg", result.get(0).getUrl());
   }

   @Test
   void getImageUrlShouldReturnValidUrl_whenKeyExists() {
      when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenReturn(HeadObjectResponse.builder().build());

      String result = imageService.getImageUrl("abc.jpg", null);
      assertEquals("http://localhost:4566/my-images/abc.jpg", result);
   }

   @Test
   void getMetadataFromUrlShouldReturnFullMetadata() {
      HeadObjectResponse head = HeadObjectResponse.builder()
            .contentType("image/png")
            .contentLength(4567L)
            .lastModified(Instant.parse("2025-01-01T00:00:00Z"))
            .build();

      when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

      String url = "http://localhost:4566/my-images/test.png";
      ImageMetadata metadata = imageService.getMetadataFromUrl(url, null);

      assertEquals("test.png", metadata.getKey());
      assertEquals(url, metadata.getUrl());
      assertEquals("image/png", metadata.getContentType());
      assertEquals(4567L, metadata.getSize());
      assertEquals(Instant.parse("2025-01-01T00:00:00Z"), metadata.getLastModified());
   }

   @Test
   void listBucketsShouldReturnBucketNames() {
      Bucket b1 = Bucket.builder().name("bucket-a").build();
      Bucket b2 = Bucket.builder().name("bucket-b").build();

      when(s3Client.listBuckets()).thenReturn(ListBucketsResponse.builder().buckets(b1, b2).build());

      List<String> result = imageService.listBuckets();

      assertTrue(result.contains("bucket-a"));
      assertTrue(result.contains("bucket-b"));
   }
}