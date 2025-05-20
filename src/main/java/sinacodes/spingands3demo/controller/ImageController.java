package sinacodes.spingands3demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sinacodes.spingands3demo.DTO.ImageMetadata;
import sinacodes.spingands3demo.DTO.UploadResponse;
import sinacodes.spingands3demo.service.ImageService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/images")
public class ImageController {

   private final ImageService imageService;

   public ImageController(ImageService imageService) {
      this.imageService = imageService;
   }

   @Operation(summary = "Upload image to S3")
   @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public ResponseEntity<UploadResponse> uploadImage(
         @RequestParam("file") MultipartFile file,
         @RequestParam(value = "bucket", required = false) String bucketOverride) {

      String key = imageService.uploadImage(file, bucketOverride);
      String url = imageService.getImageUrl(key, bucketOverride);

      return ResponseEntity.ok(new UploadResponse("Image uploaded", key, url));
   }

   @Operation(summary = "Get image by key via redirect")
   @GetMapping("/{key}/url")
   public ResponseEntity<Void> getImageUrl(@PathVariable String key) {
      String url = imageService.getImageUrl(key, null);
      return ResponseEntity.status(302).header("Location", url).build();
   }

   @Operation(summary = "Get image by key via json url")
   @GetMapping("/{key}/url-json")
   public ResponseEntity<Map<String, String>> getImageUrlJson(@PathVariable String key) {
      String url = imageService.getImageUrl(key, null);
      return ResponseEntity.ok(Map.of("url", url));
   }

   @Operation(summary = "Get image", description = "Returns image as bytes")
   @GetMapping(value = "/{key}", produces = MediaType.IMAGE_JPEG_VALUE)
   public ResponseEntity<byte[]> getImageBytes(@PathVariable String key) {
      byte[] image = imageService.getImage(key, null);
      return ResponseEntity.ok(image);
   }

   @Operation(summary = "Get image metadata from full URL")
   @GetMapping("/metadata")
   public ResponseEntity<ImageMetadata> getMetadataFromUrl(@RequestParam String url) {
      ImageMetadata metadata = imageService.getMetadataFromUrl(url, null);
      return ResponseEntity.ok(metadata);
   }


   @Operation(summary = "Delete image", description = "Deletes image by key")
   @DeleteMapping("/{key}")
   public ResponseEntity<String> deleteImage(@PathVariable String key) {
      imageService.deleteImage(key, null);
      return ResponseEntity.ok("Deleted image: " + key);
   }

   @Operation(summary = "List images", description = "Lists all image keys")
   @GetMapping
   public ResponseEntity<List<ImageMetadata>> listImages() {
      return ResponseEntity.ok(imageService.listImages(null));
   }

   @Operation(summary = "List all available S3 buckets")
   @GetMapping("/buckets")
   public ResponseEntity<List<String>> listBuckets() {
      return ResponseEntity.ok(imageService.listBuckets());
   }
}
