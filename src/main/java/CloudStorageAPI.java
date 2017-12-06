import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.storage.StorageScopes;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.CopyWriter;
import com.google.cloud.storage.StorageClass;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.rpc.Help;

import javax.swing.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Julian on 6/12/2017.
 */
public class CloudStorageAPI implements IStorage {

    private String credentials;
    private String project_id;
    private String base_uri = "https://storage.googleapis.com/";

    GoogleCredential credentials_api;

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
        //Setup credentials
        try {
            credentials_api = GoogleCredential.fromStream(new FileInputStream(getCredentials())).createScoped(Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL));

        } catch (IOException e) {
            System.out.println("Sorry API mode could not be setup. Please try again later");
        }

        return true;
    }

    public List<String> getBucketObjects (String name) {

        List<String> objects = null;

        String uri;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/" + name + "/o";
            System.out.println(uri);

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HttpRequest request = requestFactory.buildGetRequest(url);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();

            objects = parseJsonObjectList(content);

            for (int i = 0; i < objects.size(); i++) {
                System.out.println(objects.get(i));
            }

        } catch (Exception e) {
            System.out.println("Sorry there was an error with the url you entered");
        }

        return objects;
    }

    @Override
    public List<String> getBucketNames() {
        List<String> buckets = null;

        String uri;
        try {
            uri = "https://www.googleapis.com/storage/v1/b?project=" + project_id;

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HttpRequest request = requestFactory.buildGetRequest(url);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();

            buckets = parseJsonBucketList(content);

            for (int i = 0; i < buckets.size(); i++) {
                System.out.println("" + (i + 1) + ") " + buckets.get(i));
            }

        } catch (Exception e) {
            System.out.println("Sorry bucket names could not be retrieved");
        }

        return buckets;
    }

    @Override
    public int createBucket() {

        String name = Helpers.getBucketNameFromUser();
        String region = Helpers.getBucketRegionFromUser();
        StorageClass storageClass = Helpers.getStorageClassFromUser();

        System.out.println("Attempting to create a bucket named: " + name + " in " + region + ", storage class " + storageClass);

        String uri = null;
        try {
            uri = "https://www.googleapis.com/storage/v1/b?project=" + project_id;

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HashMap<String, String> new_bucket = createBucketJson(name, region, storageClass);

            HttpContent httpContent = new JsonHttpContent(new JacksonFactory(), new_bucket);

            HttpRequest request = requestFactory.buildPostRequest(url, httpContent);


            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();
            System.out.println("Bucket " + name + " successfully created using JSON API");

            addObjects(name, storageClass);

            return 0;

        } catch (UnsupportedEncodingException e) {
            System.out.println("Sorry the bucket name could not be encoded");
        } catch (GeneralSecurityException e) {
            System.out.println("Sorry there was a general security problem. Please try a different action");
        } catch (IOException e) {
            System.out.println("Sorry there was a file input problem. Please try again");
        }
        return -1;
    }

    public String createEmptyBucket() {
        String name = Helpers.getBucketNameFromUser();
        String region = Helpers.getBucketRegionFromUser();
        StorageClass storageClass = Helpers.getStorageClassFromUser();

        String uri = null;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/" + "?project=" + project_id;

            System.out.println(uri);

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HashMap<String, String> new_bucket = createBucketJson(name, region, storageClass);

            HttpContent httpContent = new JsonHttpContent(new JacksonFactory(), new_bucket);

            HttpRequest request = requestFactory.buildPostRequest(url, httpContent);


            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();

            System.out.println("Bucket " + name + " successfully created using JSON API");

            return name;

        } catch (UnsupportedEncodingException e) {
            System.out.println("Sorry the bucket name could not be encoded");
        } catch (GeneralSecurityException e) {
            System.out.println("Sorry there was a general security problem. Please try a different action");
        } catch (IOException e) {
            System.out.println("Sorry there was a file input problem. Please try again");
        }
        return "";
    }

    public void addObjects(String bucketname, StorageClass storageClass) {

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
                    uploadAFile(bucketname, storageClass, file_upload);
                } else if (file_chooser.getSelectedFile().isDirectory()) {
                    File[] files_to_upload = file_chooser.getSelectedFile().listFiles();
                    System.out.println("Uploading directory");
                    for (File file_to_upload : files_to_upload) {
                        uploadAFile(bucketname, storageClass, file_to_upload);
                    }
                }
            } else {
                System.out.println("You have not chosen a valid file to upload");
            }
        }

    }

    public boolean copyObjectToBucket(String fromBucket, String sourceObject, String destinationBucket, String destinationObject) {
        System.out.println("Attempting to copy object: " + sourceObject + " from " + fromBucket + ", to " + destinationBucket + " and rename to: " + destinationObject);

        String uri = null;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/" + fromBucket + "/o/" + sourceObject + "/copyTo/b/" + destinationBucket + "/o/" + destinationObject;
            System.out.println(uri);

            System.out.println();

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HashMap<String, String> object_transfer = new HashMap<>();

            HttpContent httpContent = new JsonHttpContent(new JacksonFactory(), object_transfer);

            HttpRequest request = requestFactory.buildPostRequest(url , httpContent);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();

            System.out.println("Object: " + sourceObject + " successfully copied");

            return true;

        } catch (UnsupportedEncodingException e) {
            System.out.println("Sorry the bucket name could not be encoded");
        } catch (GeneralSecurityException e) {
            System.out.println("Sorry there was a general security problem. Please try a different action");
        } catch (IOException e) {
            System.out.println("Sorry there was a file input problem. Please try again");
        }
        return false;

    }

    public boolean uploadAFile(String bucketname, StorageClass storageClass, File file) {
        System.out.println("Attempting to create a object: " + file.getName() + " in " + bucketname + ", storage class " + storageClass);

        String uri = null;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/sourceBucket/o/sourceObject/copyTo/b/destinationBucket/o/destinationObject";

            System.out.println();

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);


            HttpContent httpContent = new FileContent("text/plain", file);

            HttpRequest request = requestFactory.buildPostRequest(url, httpContent);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();
            System.out.println(content);

            System.out.println("Object: " + file.getName() + " successfully created using JSON API");

            return true;

        } catch (UnsupportedEncodingException e) {
            System.out.println("Sorry the bucket name could not be encoded");
        } catch (GeneralSecurityException e) {
            System.out.println("Sorry there was a general security problem. Please try a different action");
        } catch (IOException e) {
            System.out.println("Sorry there was a file input problem. Please try again");
        }
        return false;
    }

    @Override
    public boolean deleteBucket(String bucketname) {

        //Delete the contents of the bucket first
        deleteBucketContents(bucketname);

        String uri = null;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/" + URLEncoder.encode(bucketname, "UTF-8");

            System.out.println();

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HttpRequest request = requestFactory.buildDeleteRequest(url);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();
            System.out.println(content);

            System.out.println("Bucket: " + bucketname + " successfully deleted using JSON API");

            return true;

        } catch (UnsupportedEncodingException e) {
            System.out.println("Sorry the bucket name could not be encoded");
        } catch (GeneralSecurityException e) {
            System.out.println("Sorry there was a general security problem. Please try a different action");
        } catch (IOException e) {
            System.out.println("Sorry there was a file input problem. Please try again");
        }
        return false;
    }

    public void deleteBucketContents(String bucketname) {
        List<String> bucket_contents = getBucketObjects(bucketname);

        //Loop through bucket objects and delete
        for (String object : bucket_contents) {
            System.out.println("Deleting: " + object);
            deleteObject(bucketname, object);
        }
    }

    public boolean deleteObject(String bucketname, String objectname) {
        String uri = null;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/" + URLEncoder.encode(bucketname, "UTF-8") +"/o/" + URLEncoder.encode(objectname, "UTF-8");

            System.out.println();

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HttpRequest request = requestFactory.buildDeleteRequest(url);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();
            System.out.println(content);

            System.out.println("Object: " + objectname + " successfully deleted using JSON API");

            return true;

        } catch (UnsupportedEncodingException e) {
            System.out.println("Sorry the bucket name could not be encoded");
        } catch (GeneralSecurityException e) {
            System.out.println("Sorry there was a general security problem. Please try a different action");
        } catch (IOException e) {
            System.out.println("Sorry there was a file input problem. Please try again");
        }
        return false;
    }

    @Override
    public boolean splitBucket() {
        System.out.println("------ Split a Bucket ------");
        List<String> buckets = getBucketNames();

        if (buckets.size() < 1) {
            System.out.println("No buckets to split, 1 bucket must exist before splitting is possible");
            return false;
        } else {
            System.out.println("Please select a bucket to split");
            int selected_bucket = Helpers.getNumericalInput(buckets.size(), 1, true) - 1;
            System.out.println("Splitting: " + buckets.get(selected_bucket));


            //Get the user to create two new buckets for the split
            boolean creating_bucket1 = true;
            String new_bucket1 = null;
            String new_bucket2 = null;
            while (creating_bucket1) {
                System.out.println("Create your first bucket: ");
                new_bucket1 = createEmptyBucket();
                if (new_bucket1.length() > 0) {
                    creating_bucket1 = false;
                    boolean creating_bucket2 = true;
                    while (creating_bucket2) {
                        System.out.println("Create your second bucket: ");
                        new_bucket2 = createEmptyBucket();
                        if (new_bucket2.length() > 0) {
                            creating_bucket2 = false;
                        }
                    }
                }
            }

            //Get contents of the split bucket and ask user what to include in Bucket 1
            String bucket = buckets.get(selected_bucket);
            System.out.println(bucket);
            //Get all of the contents of each bucket
            List<String> objects = getBucketObjects(bucket);
            for (String object : objects) {
                List<String> options = new ArrayList<>(Arrays.asList("yes", "no"));
                boolean validating = true;
                String user_selection = "";
                while (validating) {
                    System.out.println("Would you like to add: " + object + " to " + new_bucket1 + "?");
                    System.out.println("Type 'yes' or 'no' and hit ENTER");
                    user_selection = Keyboard.readInput().toLowerCase();
                    validating = !Helpers.validateStringInput(options, user_selection);
                }
                if (user_selection.equals("yes")) {
                    System.out.println("Copying: " + object + " to " + new_bucket1);
                    copyObjectToBucket(bucket, object, new_bucket1, object);
                    System.out.println("Copied " + object + " successfully");
                } else {
                    System.out.println("Copying: " + object + " to " + new_bucket2);
                    copyObjectToBucket(bucket, object, new_bucket2, object);
                    System.out.println("Copied " + object + " successfully");

                }
            }

            //Delete the initial bucket
            System.out.println("Cleaning-up original bucket");
            deleteBucket(bucket);

            return CloudStorageJavaUI.continueMode(false);
        }
    }

    @Override
    public boolean mergeBuckets() {
        return false;
    }

    private static HashMap<String, String> createBucketJson(String name, String region, StorageClass storageClass) {
        HashMap<String, String> bucket_json = new HashMap<>();
        bucket_json.put("kind", "storage#bucket");
        bucket_json.put("name", name);
        bucket_json.put("location", region);
        bucket_json.put("storageClass", storageClass.toString());
        return bucket_json;
    }

    private static HashMap<String, String> createObjectJson(String objectName) {
        HashMap<String, String> object_json = new HashMap<>();

        object_json.put("kind", "storage#object");
        object_json.put("name", objectName);

        return object_json;
    }

    private static List<String> parseJsonObjectList(String jsonString) {
        List<String> object_names = new ArrayList<>();

        JsonElement responseJsonElement = new JsonParser().parse(jsonString);

        JsonObject responeObject = responseJsonElement.getAsJsonObject();

        JsonArray responseObjectsArray = responeObject.getAsJsonArray("items");

        for (JsonElement element : responseObjectsArray) {
            JsonObject object = element.getAsJsonObject();
            String object_name = object.get("name").getAsString();
            object_names.add(object_name);
        }

        return object_names;
    }

    private static List<String> parseJsonBucketList(String jsonString) {
        List<String> bucket_names = new ArrayList<>();

        JsonElement responseJsonElement = new JsonParser().parse(jsonString);

        JsonObject responseObject = responseJsonElement.getAsJsonObject();

        JsonArray responseObjectsArray = responseObject.getAsJsonArray("items");

        for (JsonElement element : responseObjectsArray) {
            JsonObject bucket = element.getAsJsonObject();
            String bucket_name = bucket.get("name").getAsString();
            bucket_names.add(bucket_name);
        }

        return bucket_names;

    }

}
