import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Julian on 6/12/2017.
 */
public class CloudStorageAPI implements IStorage {

    private String credentials;
    private String project_id;

    public static List<String> bucket_regions = new ArrayList<>(Arrays.asList("australia-southeast1",
            "us-central1",
            "us-east1",
            "us-east4",
            "us-west1",
            "southamerica-east1",
            "europe-west1",
            "europe-west2",
            "europe-west3",
            "asia-east1",
            "asia-northeast1",
            "asia-south1",
            "asia-southeast1"));

    public static List<StorageClass> storage_classes = new ArrayList<>(Arrays.asList(StorageClass.COLDLINE, StorageClass.DURABLE_REDUCED_AVAILABILITY, StorageClass.MULTI_REGIONAL, StorageClass.NEARLINE, StorageClass.REGIONAL, StorageClass.STANDARD));


    CloudStorageAPI(String credentials, String project_id) {
        this.credentials = credentials;
        this.project_id = project_id;

    }

    public String getCredentials() {
        return credentials;
    }

    public String getProject_id() {
        return project_id;
    }

    @Override
    public boolean setupMode() {
        return false;
    }

    @Override
    public List<String> getBucketNames() {
        return null;
    }

    @Override
    public List<Bucket> getBucketList() {
        return null;
    }

    @Override
    public int createBucket() {
        return 0;
    }

    @Override
    public Bucket createEmptyBucket() {
        return null;
    }

    @Override
    public void addObjects(Bucket bucket) {

    }

    @Override
    public boolean uploadAFile(Bucket bucket, File file) {
        return false;
    }

    @Override
    public boolean deleteBucket(String bucketname) {
        return false;
    }

    @Override
    public void deleteBucketContents(Bucket bucket) {

    }

    @Override
    public boolean splitBucket() {
        return false;
    }

    @Override
    public boolean mergeBuckets() {
        return false;
    }
}
