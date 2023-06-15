package org.resource.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import jakarta.inject.Singleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Singleton
@Component
public class AWSS3Service {

    private static final String STORAGE_REGION = Regions.US_EAST_1.getName();
    private static final int MAX_FILE_SIZE_IN_BYTES = 19000000;

    private final String storageUrl;
    private final String storageServiceUrl;
    private final AmazonS3 s3client;

    @Autowired
    public AWSS3Service(Properties properties) {
        storageUrl = properties.getStorageUrl();
        storageServiceUrl = properties.getStorageServiceUrl();
        this.s3client = initializeAWSS3Storage();
    }

    //is bucket exist?
    public boolean doesBucketExist(String bucketName) {
        return s3client.doesBucketExistV2(bucketName);
    }

    //create a bucket
    public Bucket createBucket(String bucketName) {
        return s3client.createBucket(bucketName);
    }

    //list all buckets
    public List<Bucket> listBuckets() {
        return s3client.listBuckets();
    }

    //delete a bucket
    public void deleteBucket(String bucketName) {
        s3client.deleteBucket(bucketName);
    }

    //uploading object
    public PutObjectResult putObject(String bucketName, String key, InputStream input) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, input, new ObjectMetadata());
        putObjectRequest.getRequestClientOptions().setReadLimit(MAX_FILE_SIZE_IN_BYTES);
        return s3client.putObject(putObjectRequest);
    }

    //listing objects
    public ObjectListing listObjects(String bucketName) {
        return s3client.listObjects(bucketName);
    }

    //get an object
    public S3Object getObject(String bucketName, String objectKey) {
        return s3client.getObject(bucketName, objectKey);
    }

    //copying an object
    public CopyObjectResult copyObject(
            String sourceBucketName,
            String sourceKey,
            String destinationBucketName,
            String destinationKey
    ) {
        return s3client.copyObject(
                sourceBucketName,
                sourceKey,
                destinationBucketName,
                destinationKey
        );
    }

    //deleting an object
    public void deleteObject(String bucketName, String objectKey) {
        s3client.deleteObject(bucketName, objectKey);
    }

    //deleting multiple Objects
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest delObjReq) {
        return s3client.deleteObjects(delObjReq);
    }

    private AmazonS3 initializeAWSS3Storage() {
        return s3client != null
                ? s3client
                : AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(storageUrl, STORAGE_REGION))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("awsAccessKey", "awsSecretKey")))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public String getStorageServiceUrl() {
        return storageServiceUrl;
    }

}
