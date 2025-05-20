package sinacodes.spingands3demo.DTO;

public class UploadResponse {
   private final String key;
   private final String url;
   private final String message;

   public UploadResponse(String key, String url, String message) {
      this.key = key;
      this.url = url;
      this.message = message;
   }

   public String getKey() {
      return key;
   }

   public String getUrl() {
      return url;
   }

   public String getMessage() {
      return message;
   }
}