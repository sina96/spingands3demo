package sinacodes.spingands3demo.DTO;

public class ImageMetadata {
   private String key;
   private String url;

   public ImageMetadata(String key, String url) {
      this.key = key;
      this.url = url;
   }

   public String getKey() {
      return key;
   }

   public String getUrl() {
      return url;
   }
}
