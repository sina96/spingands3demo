package sinacodes.spingands3demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

   @Value("${aws.region}")
   private String region;

   @Value("${aws.s3.access-key}")
   private String accessKey;

   @Value("${aws.s3.secret-key}")
   private String secretKey;

   @Value("${aws.s3.endpoint}")
   private String endpoint;

   @Bean
   public S3Client s3Client() {
      return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                  StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                  )
            )
            .endpointOverride(URI.create(endpoint))
            .serviceConfiguration(S3Configuration.builder()
                  .pathStyleAccessEnabled(true)
                  .build())
            .build();
   }
}
