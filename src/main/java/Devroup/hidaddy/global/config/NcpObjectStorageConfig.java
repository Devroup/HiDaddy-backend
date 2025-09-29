package Devroup.hidaddy.global.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NcpObjectStorageConfig {

    @Value("${ncp.access-key}") private String accessKey;
    @Value("${ncp.secret-key}") private String secretKey;

    // NCP Object Storage 고정 값
    private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";
    private static final String REGION   = "kr-standard";

    @Bean
    public AmazonS3 ncpS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT, REGION))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)   // NCP 권장
                .disableChunkedEncoding()           // 일부 환경에서 호환성↑
                .build();
    }
}
