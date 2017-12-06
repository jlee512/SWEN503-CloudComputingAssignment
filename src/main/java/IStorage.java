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

    public boolean setupMode();

    public boolean deleteBucket(String bucketname);

    public boolean splitBucket();

    public boolean mergeBuckets();
}
