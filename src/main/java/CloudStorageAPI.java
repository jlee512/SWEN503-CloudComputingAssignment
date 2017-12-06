import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.storage.StorageScopes;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageClass;
import com.google.gson.JsonObject;
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
            e.printStackTrace();
        }

        return true;
    }

    public void getBucket(String name) {

        String uri;
        try {
            uri = "https://www.googleapis.com/storage/v1/b/" + URLEncoder.encode(name, "UTF-8") + "/o";

            // Include your credentials in the Authorization header of the HTTP request
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credentials_api);
            GenericUrl url = new GenericUrl(uri);

            HttpRequest request = requestFactory.buildGetRequest(url);

            // Execute the HTTP request
            HttpResponse response = request.execute();
            String content = response.parseAsString();

            System.out.println(content);


            System.out.println("---- jsonAPI ----\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
        return -1;
    }

    public String createEmptyBucket() {
        String name = Helpers.getBucketNameFromUser();
        String region = Helpers.getBucketRegionFromUser();
        StorageClass storageClass = Helpers.getStorageClassFromUser();

        System.out.println("Attempting to create a bucket named: " + name + " in " + region + ", storage class " + storageClass);

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
            e.printStackTrace();
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

    public boolean uploadAFile(String bucketname, StorageClass storageClass, File file) {
        System.out.println("Attempting to create a object: " + file.getName() + " in " + bucketname + ", storage class " + storageClass);

        String uri = null;
        try {
            uri = "https://www.googleapis.com/upload/storage/v1/b/" + URLEncoder.encode(bucketname, "UTF-8") + "/o" + "?uploadType=media&name=" + file.getName();

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
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBucket(String bucketname) {
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
            e.printStackTrace();
        }
        return false;
    }

    public void deleteBucketContents(String bucketname) {

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
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean splitBucket() {
        return false;
    }

    @Override
    public boolean mergeBuckets() {
        return false;
    }

    private static void prettyPrintXml(final String bucketName, final String content) {

        // Instantiate transformer input.
        Source xmlInput = new StreamSource(new StringReader(content));
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        // Configure transformer.
        try {

            Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity transformer
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);

            // Pretty print the output XML.
            System.out.println("\nBucket listing for " + bucketName + ":\n");
            System.out.println(xmlOutput.getWriter().toString());
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> createBucketJson(String name, String region, StorageClass storageClass) {
        HashMap<String, String> bucket_json = new HashMap<>();
        bucket_json.put("kind", "storage#bucket");
        bucket_json.put("name", name);
        bucket_json.put("location", region);
        bucket_json.put("storageClass", storageClass.toString());
        return bucket_json;
    }

    public static String processMD5MessageDigest(byte[] file_bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return Base64.getEncoder().encodeToString(file_bytes);

        } catch (NoSuchAlgorithmException e) {
            System.out.println("MD5 hashing algorithm not found");
        }

        return "";
    }

    public static void main(String[] args) {
        System.out.println(createBucketJson("test", "testregion", StorageClass.COLDLINE));
    }
}
