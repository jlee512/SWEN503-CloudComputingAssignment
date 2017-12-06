import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Julian on 5/12/2017.
 */
public class CloudStorageSDK implements IStorage {

    private String credentials;
    private String project_id;

    StorageOptions.Builder builder;
    private static Storage storage;

    CloudStorageSDK(String credentials, String project_id) {
        this.credentials = credentials;
        this.project_id = project_id;

    }

    public String getCredentials() {
        return credentials;
    }

    public String getProject_id() {
        return project_id;
    }

    public boolean setupMode() {
        builder = StorageOptions.newBuilder();
        //Set project_id
        builder.setProjectId(project_id);

        //Verify credentials
        try {
            builder.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentials)));
            storage = builder.build().getService();
            return true;
        } catch (IOException e) {
            //Theoretically should not happen using file navigator
            System.out.println("Your credentials file could not be found");
            return false;
        }
    }

    @Override
    public List<String> getBucketNames() {
        Page<Bucket> buckets = storage.list(Storage.BucketListOption.pageSize(100), Storage.BucketListOption.prefix(""));
        List<String> bucket_list = new ArrayList<>();

        for (Bucket bucket : buckets.iterateAll()) {
            bucket_list.add(bucket.getName());
        }

        return bucket_list;

    }

    public List<Bucket> getBucketList() {
        Page<Bucket> buckets = storage.list(Storage.BucketListOption.pageSize(100), Storage.BucketListOption.prefix(""));
        List<Bucket> bucket_list = new ArrayList<>();

        int i = 1;
        System.out.println("Buckets");
        for (Bucket bucket : buckets.iterateAll()) {
            bucket_list.add(bucket);
            System.out.println("" + i + ") " + bucket.getName());
            i++;
        }

        return bucket_list;

    }

    @Override
    public int createBucket() {

        String name = Helpers.getBucketNameFromUser();
        String region = Helpers.getBucketRegionFromUser();
        StorageClass storageClass = Helpers.getStorageClassFromUser();

        System.out.println("Attempting to create a bucket named: " + name + " in " + region + ", storage class " + storageClass);

        try {
            Bucket bucket = storage.create(BucketInfo.newBuilder(name)
                    // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
                    .setStorageClass(storageClass)
                    // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
                    .setLocation(region)
                    .build());
            System.out.printf("\tBucket %s created.%n", bucket.getName() + " in " + region + ", storage class " + storageClass);

            addObjects(bucket);

            return 0;
        } catch (StorageException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public Bucket createEmptyBucket() {

        String name = Helpers.getBucketNameFromUser();
        String region = Helpers.getBucketRegionFromUser();
        StorageClass storageClass = Helpers.getStorageClassFromUser();

        System.out.println("Attempting to create a bucket named: " + name + " in " + region + ", storage class " + storageClass);

        try {
            Bucket bucket = storage.create(BucketInfo.newBuilder(name)
                    // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
                    .setStorageClass(storageClass)
                    // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
                    .setLocation(region)
                    .build());
            System.out.printf("\tBucket %s created.%n", bucket.getName() + " in " + region + ", storage class " + storageClass);


            return bucket;
        } catch (StorageException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void addObjects(Bucket bucket) {

        List<String> valid_inputs = new ArrayList<>();
        valid_inputs.add("yes");
        valid_inputs.add("no");
        boolean valid_input = false;
        String add_objects_input = "";
        while (!valid_input) {
            System.out.println("Would you like to add objects to this bucket?");
            System.out.println("---- Type 'yes' or 'no' and hit ENTER ----");
            add_objects_input = Keyboard.readInput().toLowerCase();
            valid_input = Helpers.validateStringInput(valid_inputs, add_objects_input);
        }

        if (add_objects_input.equals("yes")) {
            //Choose files or list of files from a directory
            JFileChooser file_chooser = new JFileChooser();
            String project_directory = System.getProperty("user.dir");
            file_chooser.setCurrentDirectory(new File(project_directory));
            file_chooser.setDialogTitle("Choose the files you would like to upload");
            file_chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            if (file_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                if (file_chooser.getSelectedFile().isFile()) {
                    File file_upload = file_chooser.getSelectedFile();
                    System.out.println("Uploading individual file");
                    uploadAFile(bucket, file_upload);
                } else if (file_chooser.getSelectedFile().isDirectory()) {
                    File[] files_to_upload = file_chooser.getSelectedFile().listFiles();
                    System.out.println("Uploading directory");
                    for (File file_to_upload : files_to_upload) {
                        uploadAFile(bucket, file_to_upload);
                    }
                }
            } else {
                System.out.println("You have not chosen a valid file to upload");
            }
        }
    }

    public boolean uploadAFile(Bucket bucket, File file) {

        String blobName = file.getName();
        byte[] file_bytes = Helpers.processFileToBytes(file);
        if (file_bytes != null) {
            InputStream content = null;
            content = new ByteArrayInputStream(file_bytes);
            Blob blob = bucket.create(blobName, content, "text/plain");
            System.out.println(blobName + " has been uploaded successfully");
            return true;
        } else {
            System.out.println("File could not be converted to bytes");
            return false;
        }

    }

    @Override
    public boolean deleteBucket(String bucketname) {

        Page<Bucket> buckets = storage.list(Storage.BucketListOption.pageSize(100), Storage.BucketListOption.prefix(""));
        List<String> bucket_list = new ArrayList<>();

        for (Bucket bucket : buckets.iterateAll()) {
            if (bucket.getName().equals(bucketname)) {
                //Get all items in the bucket
                deleteBucketContents(bucket);

                //Delete the bucket itself
                System.out.println("Deleting: " + bucket.getName());
                bucket.delete();
            }
        }
        return true;
    }

    public void deleteBucketContents(Bucket bucket) {
        Page<Blob> blobs = bucket.list();
        for (Blob blob : blobs.iterateAll()) {
            System.out.println("Deleting object: " + blob.getBlobId().getName());
            storage.delete(blob.getBlobId());
        }
    }

    @Override
    public boolean splitBucket() {
        System.out.println("------ Split a Bucket ------");
        List<Bucket> buckets = getBucketList();

        if (buckets.size() < 1) {
            System.out.println("No buckets to split, 1 bucket must exist before splitting is possible");
            return false;
        } else {
            System.out.println("Please select a bucket to split");
            int selected_bucket = Helpers.getNumericalInput(buckets.size(), 1, true) - 1;
            System.out.println("Splitting: " + buckets.get(selected_bucket).getName());


            //Get the user to create two new buckets for the split
            boolean creating_bucket1 = true;
            Bucket new_bucket1 = null;
            Bucket new_bucket2 = null;
            while (creating_bucket1) {
                System.out.println("Create your first bucket: ");
                new_bucket1 = createEmptyBucket();
                if (new_bucket1 != null) {
                    creating_bucket1 = false;
                    boolean creating_bucket2 = true;
                    while (creating_bucket2) {
                        System.out.println("Create your second bucket: ");
                        new_bucket2 = createEmptyBucket();
                        if (new_bucket2 != null) {
                            creating_bucket2 = false;
                        }
                    }
                }
            }

            //Get contents of the split bucket and ask user what to include in Bucket 1
            Bucket bucket = buckets.get(selected_bucket);
            //Get all of the contents of each bucket
            Page<Blob> blobs = bucket.list();
            for (Blob blob_item : blobs.iterateAll()) {
                List<String> options = new ArrayList<>(Arrays.asList("yes", "no"));
                boolean validating = true;
                String user_selection = "";
                while (validating) {
                    System.out.println("Would you like to add: " + blob_item.getBlobId().getName() + " to " + new_bucket1.getName() + "?");
                    System.out.println("Type 'yes' or 'no' and hit ENTER");
                    user_selection = Keyboard.readInput().toLowerCase();
                    validating = !Helpers.validateStringInput(options, user_selection);
                }
                if (user_selection.equals("yes")) {
                    System.out.println("Copying: " + blob_item.getBlobId().getName() + " to " + new_bucket1.getName());
                    CopyWriter copyWriter = blob_item.copyTo(new_bucket1.getName(), blob_item.getName());
                    Blob copiedBlob = copyWriter.getResult();
                } else {
                    System.out.println("Copying: " + blob_item.getBlobId().getName() + " to " + new_bucket2.getName());
                    CopyWriter copyWriter = blob_item.copyTo(new_bucket2.getName(), blob_item.getName());
                    Blob copiedBlob = copyWriter.getResult();
                }
            }

            //Delete the initial bucket
            System.out.println("Cleaning-up original bucket");
            deleteBucket(buckets.get(selected_bucket).getName());

            return CloudStorageJavaUI.continueMode(false);
        }
    }

    @Override
    public boolean mergeBuckets() {
        System.out.println("------ Merging Buckets ------");
        List<Bucket> buckets = getBucketList();

        if (buckets.size() < 2) {
            System.out.println("No buckets to merge, add at least two buckets before merging");
            return false;
        } else {
            System.out.println("Please select buckets to merge:");
            List<Integer> selected_buckets = new ArrayList<>();

            boolean selecting = true;

            while (selecting) {
                System.out.println("Enter a bucket number to merge and hit ENTER");
                int selection = Helpers.getNumericalInput(buckets.size(), 1, true) - 1;
                if (!selected_buckets.contains(selection)) {
                    selected_buckets.add(selection);
                }

                if (selected_buckets.size() < 2) {
                    System.out.println("Please select another bucket");
                } else {
                    if (selected_buckets.size() < buckets.size()) {
                        boolean validating = true;
                        while (validating) {
                            System.out.println("Add more buckets?");
                            System.out.println("Please type 'yes' or 'no' and hit ENTER");
                            List<String> options = new ArrayList<>(Arrays.asList("yes", "no"));
                            String user_selection = Keyboard.readInput().toLowerCase();
                            validating = !Helpers.validateStringInput(options, user_selection);
                            if (!validating) {
                                if (user_selection.equals("yes")) {
                                    selecting = true;
                                } else {
                                    selecting = false;
                                }
                            }
                        }
                    } else {
                        System.out.println("No more buckets are available");
                        selecting = false;
                    }

                }
            }


            List<Blob> contents = new ArrayList<>();
            for (int i = 0; i < selected_buckets.size(); i++) {
                int bucket_index = selected_buckets.get(i);
                Bucket bucket = buckets.get(bucket_index);
                System.out.println("Merging: " + bucket.getName());
                //Get all of the contents of each bucket
                Page<Blob> blobs = bucket.list();
                for (Blob blob : blobs.iterateAll()) {
                    contents.add(blob);
                }
            }

            //Get the user to create a new bucket for the merge
            boolean creating_bucket = true;
            Bucket new_bucket = null;
            while (creating_bucket) {
                new_bucket = createEmptyBucket();
                if (new_bucket != null) {
                    creating_bucket = false;
                } else {
                    creating_bucket = true;
                }
            }

            //For all of the Blobs, move from directories into target (i.e. copy then delete
            System.out.println("Moving objects from merging buckets to new bucket");
            for (int i = 0; i < contents.size(); i++) {
                Blob blob_item = contents.get(i);
                CopyWriter copyWriter = blob_item.copyTo(new_bucket.getName(), blob_item.getName());
                Blob copiedBlob = copyWriter.getResult();
            }

            //Delete buckets once contents have been moved
            for (int i = 0; i < selected_buckets.size(); i++) {
                String bucketName = buckets.get(selected_buckets.get(i)).getName();
                deleteBucket(bucketName);
            }

            return CloudStorageJavaUI.continueMode(false);
        }
    }


}
