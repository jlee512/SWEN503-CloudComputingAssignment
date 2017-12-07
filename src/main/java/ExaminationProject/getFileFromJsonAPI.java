package ExaminationProject;
import RegularProject.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Julian on 7/12/2017.
 */
public class getFileFromJsonAPI {



    public static void main(String[] args) {

        boolean session = true;
        //Get credentials
        System.out.println("Please choose your credentials JSON file: ");
        String credentials_path = "";
        String project_id = "";
        //Get user credentials JSON file
        credentials_path = CloudStorageJavaUI.getCredentialsJSONPath();
        //Get project id
        project_id = CloudStorageJavaUI.getProjectId();
        //To comment out
        System.out.println("!!!DEFAULT PROJECT NAME INPUT FOR TESTING - COMMENT TO REMOVE!!!");
        project_id = "SWEN503-CloudComputing";

        while (session) {
            System.out.println("***** You have connected to Google Cloud Storage *****");
            //Prompt the user to choose the SDK or API approach
            String api_sdk_choice = "api";

            switch (api_sdk_choice) {
                case "api":
                    CloudStorageAPI api_mode = new CloudStorageAPI(credentials_path, project_id);
                    int api_return_code = setupSelectedMode(api_mode);
                    if (api_return_code == 200) {
                        return;
                    }
                    api_sdk_choice = "quit";
                    break;
            }
        }
    }

    public static int setupSelectedMode (IStorage mode) {
        boolean continue_sdk_mode = true;
        int status_code = -1;

        if (mode.setupMode()) {
            while (continue_sdk_mode) {
                System.out.println("Downloading text file from JSON API");
                boolean result = mode.downloadFile();
                if (result) {
                    return 200;
                }
            }
            return 0;
        } else {
            System.out.println("Sorry your SDK configuration could not setup successfully");
            System.out.println("Please try again");
            return 1;

        }
    }


}
