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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceTest {

   private S3Client s3Client;
   private ImageService imageService;

   @BeforeEach
   void setUp() {
      s3Client = mock(S3Client.class);
      imageService = new ImageService(s3Client);
      imageService.getClass(); // to suppress IDE warnings
      // Set required fields manually (normally injected by Spring)
      imageService.getClass().getDeclaredFields();
      setField(imageService, "bucket", "test-bucket");
      setField(imageService, "endpoint", "http://localhost:4566");
   }

   private void setField(Object target, String fieldName, String value) {
      try {
         var field = target.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(target, value);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Test
   void uploadImage_shouldReturnGeneratedKey() throws IOException {
      MockMultipartFile file = new MockMultipartFile(
            "file", "image.jpg", "image/jpeg", "fake-image".getBytes()
      );

      PutObjectResponse mockResponse = PutObjectResponse.builder().build();
      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

      String key = imageService.uploadImage(file);

      assertTrue(key.endsWith(".jpg"));
      assertDoesNotThrow(() -> UUID.fromString(key.substring(0, key.indexOf('.'))));
      verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
   }

   @Test
   void getImage_shouldReturnBytes() throws IOException {
      byte[] mockBytes = "image-bytes".getBytes();
      ResponseInputStream<GetObjectResponse> mockStream = new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            new ByteArrayInputStream(mockBytes)
      );

      when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

      byte[] result = imageService.getImage("abc.jpg");

      assertArrayEquals(mockBytes, result);
   }

   @Test
   void updateImage_shouldCallPutObject() throws IOException {
      MockMultipartFile file = new MockMultipartFile(
            "file", "updated.jpg", "image/jpeg", "new-image".getBytes()
      );

      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

      imageService.updateImage(file, "abc.jpg");

      verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
   }

   @Test
   void deleteImage_shouldCallDeleteObject() {
      imageService.deleteImage("abc.jpg");
      verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
   }

   @Test
   void getImageUrl_shouldReturnCorrectUrl() {
      String url = imageService.getImageUrl("abc.jpg");
      assertEquals("http://localhost:4566/test-bucket/abc.jpg", url);
   }

   @Test
   void listImages_shouldReturnMetadataList() {
      S3Object obj1 = S3Object.builder().key("a.jpg").build();
      S3Object obj2 = S3Object.builder().key("b.png").build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(ListObjectsV2Response.builder().contents(obj1, obj2).build());

      List<ImageMetadata> result = imageService.listImages();

      assertEquals(2, result.size());
      assertEquals("a.jpg", result.get(0).getKey());
      assertTrue(result.get(0).getUrl().endsWith("/a.jpg"));
   }
}