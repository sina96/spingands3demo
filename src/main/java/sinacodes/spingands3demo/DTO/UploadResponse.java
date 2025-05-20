package sinacodes.spingands3demo.DTO;

public class UploadResponse extends ImageMetadata {
   private String message;

   public UploadResponse(String message, String key, String url) {
      super(key, url);
      this.message = message;
   }

   public String getMessage() {
      return message;
   }
}