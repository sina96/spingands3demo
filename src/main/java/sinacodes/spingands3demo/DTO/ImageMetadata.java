package sinacodes.spingands3demo.DTO;

import java.time.Instant;

public class ImageMetadata {
   private String key;
   private String url;
   private String contentType;
   private long size;
   private Instant lastModified;

   public ImageMetadata(String key, String url, String contentType, long size, Instant lastModified) {
      this.key = key;
      this.url = url;
      this.contentType = contentType;
      this.size = size;
      this.lastModified = lastModified;
   }

   public String getKey() { return key; }
   public String getUrl() { return url; }
   public String getContentType() { return contentType; }
   public long getSize() { return size; }
   public Instant getLastModified() { return lastModified; }
}
