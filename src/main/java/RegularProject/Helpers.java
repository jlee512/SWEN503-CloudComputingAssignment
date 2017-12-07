package RegularProject;

import com.google.cloud.storage.StorageClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Julian on 5/12/2017.
 */
public class Helpers {

    public static final List<String> BUCKET_REGIONS = new ArrayList<>(Arrays.asList("australia-southeast1",
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

    public static final List<StorageClass> STORAGE_CLASSES = new ArrayList<>(Arrays.asList(StorageClass.COLDLINE, StorageClass.DURABLE_REDUCED_AVAILABILITY, StorageClass.MULTI_REGIONAL, StorageClass.NEARLINE, StorageClass.REGIONAL, StorageClass.STANDARD));

    public static boolean validateStringInput(List<String> valid_inputs_list, String user_input) {
        return valid_inputs_list.contains(user_input.toLowerCase());
    }

    public static int getNumericalInput(int max, int min, boolean useRange) {

        boolean valid_number_input = false;

        while (!valid_number_input) {
            String raw_input = Keyboard.readInput();
            try {
                int number_input = Integer.parseInt(raw_input);
                if (useRange) {
                    if (number_input < min || number_input > max) {
                        throw new InvalidNumberInputException("Your input was not a menu option, please try again");
                    }
                }
                return number_input;
            } catch (NumberFormatException e) {
                System.out.println("You have not entered a number. please try again");
            } catch (InvalidNumberInputException e) {
                System.out.println("You entered an option outside of the number range provided, please try again");
            }

        }
        return -1;
    }

    public static boolean getNonBlankStringInput(String input) {

        if (input.length() == 0) {
            System.out.println("You did not enter anything, please try again...");
            return false;
        } else {
            return true;
        }

    }

    public static String getBucketNameFromUser() {

        boolean bucket_created = false;

        while (!bucket_created) {

            System.out.print("Please enter a name for your bucket:  ");
            String bucket_name_attempt = Keyboard.readInput();

            if (bucket_name_attempt.length() == 0) {
                System.out.println("Bucket name not entered, please try again");
            } else {
                return bucket_name_attempt;
            }
        }

        return "";
    }

    public static String getBucketRegionFromUser() {

        System.out.println("Please select a bucket region: ");
        for (int i = 0; i < BUCKET_REGIONS.size(); i++) {
            System.out.println("" + (i + 1) + ") " + BUCKET_REGIONS.get(i));
        }
        System.out.println("Please type the number of the region you would like and hit ENTER");

        int selection = Helpers.getNumericalInput(BUCKET_REGIONS.size(), 1, true);

        return BUCKET_REGIONS.get(selection - 1);

    }

    public static StorageClass getStorageClassFromUser() {

        System.out.println("Please select a storage class: ");
        for (int i = 0; i < STORAGE_CLASSES.size(); i++) {
            System.out.println("" + (i + 1) + ") " + STORAGE_CLASSES.get(i).name());
        }
        System.out.println("Please type the number of the storage class you would like and hit ENTER");

        int selection = Helpers.getNumericalInput(STORAGE_CLASSES.size(), 1, true);

        return STORAGE_CLASSES.get(selection - 1);

    }


    public static byte[] processFileToBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.out.println("File bytes could not be read");
            return null;
        }

    }

}
