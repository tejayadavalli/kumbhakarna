package kumbhakarna;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;

public class S3Uploader {
    private final AmazonS3 s3Client;

    public S3Uploader() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAYHJPW3MPO42LDHYV",
                "fIb+8gV8g9liyOJaA0AutGWvd9CBxrRAm08WG5IT");
        s3Client = AmazonS3ClientBuilder.standard().withCredentials(new
                AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.AP_SOUTH_1).build();
    }

    public URL generatePreSignedUrl(String fileName, String method) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 10 * 60;
        expiration.setTime(expTimeMillis);
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest("kumbhakarna-bills", fileName)
                        .withMethod(method.equals("PUT") ? HttpMethod.PUT: HttpMethod.GET)
                        .withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url;
    }
}
