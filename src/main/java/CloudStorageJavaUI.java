import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Julian on 5/12/2017.
 */
public class CloudStorageJavaUI {

    public static void main(String[] args) {

        boolean session = true;
        //Print welcome screen
        startupScreen();

        System.out.println("Please choose your credentials JSON file: ");
        String credentials_path = "";
        String project_id = "";
        //Get user credentials JSON file
        credentials_path = getCredentialsJSONPath();
        //Get project id
        project_id = getProjectId();
        //To comment out
        System.out.println("!!!DEFAULT PROJECT NAME INPUT FOR TESTING - COMMENT TO REMOVE!!!");
        project_id = "SWEN503-CloudComputing";

        while (session) {
            System.out.println("***** You have connected to Google Cloud Storage *****");
            //Prompt the user to choose the SDK or API approach
            String api_sdk_choice = chooseAPIorSDK();

            switch (api_sdk_choice) {
                case "sdk":

                    //Authenticate project_id and credentials
                    CloudStorageSDK sdk_mode = new CloudStorageSDK(credentials_path, project_id);
                    int sdk_return_code = setupSelectedMode(sdk_mode);
                    continue;
                case "api":
                    CloudStorageAPI api_mode = new CloudStorageAPI(credentials_path, project_id);
                    int api_return_code = setupSelectedMode(api_mode);
                    continue;
                case "quit":
                    System.out.println("Thanks for using the Google Cloud Storage Bucket Manager");
                    session = false;
                    return;
            }
        }

    }

    static int setupSelectedMode (IStorage mode) {
        boolean continue_sdk_mode = true;
        int status_code = -1;

        if (mode.setupMode()) {
            while (continue_sdk_mode) {

                List<String> options = new ArrayList<>(Arrays.asList("split", "merge", "create", "exit"));
                System.out.println("What would you like to do?");
                System.out.println("----- Split -----");
                System.out.println("----- Merge -----");
                System.out.println("----- Create -----");
                System.out.println("--- Exit SDK Mode ---");
                System.out.println("Type 'split', 'merge', 'create' or 'exit' and hit ENTER");

                String user_choice = Keyboard.readInput().toLowerCase();
                boolean choice_validation = Helpers.validateStringInput(options, user_choice);

                if (choice_validation) {
                    switch (user_choice) {
                        case "create":
                            continue_sdk_mode = createBucketUI(mode, continue_sdk_mode);
                            break;
                        case "split":
                            continue_sdk_mode = mode.splitBucket();
                            break;
                        case "merge":
                            continue_sdk_mode = mode.mergeBuckets();
                            break;
                        case "exit":
                            continue_sdk_mode = false;
                            break;
                    }

                } else {
                    System.out.println("You did not choose a valid option. Please try again...");
                }


            }
            return 0;
        } else {
            System.out.println("Sorry your SDK configuration could not setup successfully");
            System.out.println("Please try again");
            return 1;

        }
    }

    public static boolean createBucketUI(IStorage mode, boolean continue_sdk_mode) {
        int status = mode.createBucket();

        if (status == 0) {

            continue_sdk_mode = continueMode(continue_sdk_mode);
        }
        return continue_sdk_mode;
    }

    public static boolean continueMode(boolean continue_mode) {
        String continue_sdk = "";
        List<String> valid_inputs = new ArrayList<>();
        valid_inputs.add("yes");
        valid_inputs.add("no");
        boolean valid_mode_continue = false;
        while (!valid_mode_continue) {
            System.out.print("Would you like to continue in mode? Type 'Yes' or 'No' and hit ENTER:  ");
            continue_sdk = Keyboard.readInput().toLowerCase();
            valid_mode_continue = Helpers.validateStringInput(valid_inputs, continue_sdk);
        }

        if (!continue_sdk.equals("yes")) {
            continue_mode = false;
        } else {
            continue_mode = true;
        }
        return continue_mode;
    }


    static void startupScreen() {

        System.out.println("Welcome to the Google Cloud Storage Bucket Manager");
        System.out.println("-------------------------------------------------------------");

    }

    static String getCredentialsJSONPath() {

        boolean json_file_selected = false;

        while (!json_file_selected) {
            //Use filechooser GUI to allow the user to select the credentials JSON file
            JFileChooser file_chooser = new JFileChooser();
            String project_directory = System.getProperty("user.dir");
            file_chooser.setCurrentDirectory(new File(project_directory));
            file_chooser.setDialogTitle("Navigate to your google cloud storage JSON credentials");
            file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            file_chooser.setFileFilter(new JsonFileFilter());

            if (file_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION && file_chooser.getSelectedFile().getAbsoluteFile().getName().endsWith(".json")) {
                return file_chooser.getSelectedFile().getAbsolutePath();
            } else {
                System.out.println("You have not chosen a JSON file, please try again");
                file_chooser.setDialogTitle("You have not chosen a JSON file, please try again");
            }
        }
        //Theoretically never reached
        return null;
    }

    static String getProjectId() {

        boolean project_id_entered = false;

        while (!project_id_entered) {
            //Prompt user for projectId input
            System.out.print("Please enter your project id and press ENTER:  ");
            String project_id_raw = Keyboard.readInput();

            boolean verify_projectId_input = Helpers.getNonBlankStringInput(project_id_raw);

            if (verify_projectId_input) {
                return project_id_raw;
            }
        }

        //Theoretically would never be reached, return null
        return null;

    }

    static String chooseAPIorSDK() {

        boolean valid_user_input = false;

        //Prompt user to choose the SDK approach or the API approach
        while (!valid_user_input) {

            System.out.println("Would you like to use the Google Cloud Storage SDK or API?");
            System.out.println("---- Type 'SDK' or 'API' and hit ENTER ----");
            System.out.println("---- To quit, type 'quit' and hit ENTER ---");

            List<String> valid_inputs = new ArrayList<>(Arrays.asList("sdk", "api", "quit"));
            String raw_input = Keyboard.readInput().toLowerCase();

            if (Helpers.validateStringInput(valid_inputs, raw_input)) {
                return raw_input;
            } else {
                System.out.println("Sorry, your input was not 'SDK', 'API' or 'QUIT'");
                System.out.println();
            }
        }
        //Theoretically would never be reached, return null
        return null;
    }
}
