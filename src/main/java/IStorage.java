import com.google.cloud.storage.Bucket;

import java.io.File;
import java.util.List;

/**
 * Created by Julian on 6/12/2017.
 */
public interface IStorage {

    public List<String> getBucketNames();

    public List<Bucket> getBucketList();

    public int createBucket();

    public Bucket createEmptyBucket();

    public void addObjects(Bucket bucket);

    public boolean uploadAFile(Bucket bucket, File file);

    public boolean deleteBucket(String bucketname);

    public void deleteBucketContents(Bucket bucket);

    public boolean splitBucket();

    public boolean mergeBuckets();
}
