import io.github.bobfrostman.zephyr.ZephyrAPI;
import io.github.bobfrostman.zephyr.client.IZephyrProjectApiClient;
import io.github.bobfrostman.zephyr.client.response.*;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCase;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseStatus;

public class Example {

    public static void main(String[] args) {
        String token = args[0];
        String projectKey = args[1];

        IZephyrProjectApiClient client = ZephyrAPI.createClient()
                .withApiUrl("https://api.zephyrscale.smartbear.com/v2")
                .withToken(token)
                .withProjectKey(projectKey)
                .build();

        GetStatusesResponse statusesResponse = client.getStatuses();
        if (statusesResponse.isSuccessful()) {
            for (ZephyrTestCaseStatus status : statusesResponse.getStatuses()) {
                System.out.println(status.getName() + " (ID: " + status.getId() + ")");
            }
        } else {
            System.err.println("Failed to retrieve statuses: " + statusesResponse.getErrorMessage());
        }

        CreateFolderResponse createFolderResponse = client.newTestCaseFolder()
                .withParentId(22138712l)
                .withName("New Test Case Folder")
                .create();

        if (createFolderResponse.isSuccessful()) {
            System.out.println("Folder created successfully with ID: " + createFolderResponse.getCreatedFolder().getId());
        } else {
            System.err.println("Failed to create folder: " + createFolderResponse.getErrorMessage());
        }

        CreateTestCaseResponse createResponse = client.newTestCase()
                .withFolderPath("/mz folder/MKT/manila/Autocreated Folder/")
                .withName("Auto Test Case")
                .withObjective("Verify important functionality")
                .withPrecondition("Environment needs to be set up")
                .withStatusName("Approved")
                .withPriorityName("High")
                .withStep("Given Perform action X")
                .withStep("Then Verify that result Y is as expected")
                .create();

        if (createResponse.isSuccessful()) {
            System.out.println("Test case created successfully with ID: " + createResponse.getCreatedTestCase().getKey());
        } else {
            System.err.println("Failed to create test case: " + createResponse.getErrorMessage());
        }

        String testCaseKey = createResponse.getCreatedTestCase().getKey();
        GetTestCaseResponse testCaseResponse = client.getTestCase(testCaseKey, true);

        if (testCaseResponse.isSuccessful()) {
            ZephyrTestCase testCase = testCaseResponse.getTestCase();
            System.out.println("Test Case Name: " + testCase.getName());
            if (testCase.getSteps() != null) {
                System.out.println("Steps:");
                for (String step : testCase.getSteps()) {
                    System.out.println("- " + step);
                }
            }
        } else {
            System.err.println("Failed to retrieve test case: " + testCaseResponse.getErrorMessage());
        }

        UpdateTestCaseResponse updateTestCaseResponse = client.updateTestCase(testCaseKey).withName("Another name")
                .withStep("Given steps overridden")
                .withPriorityName("Draft")
                .withObjective("Modify test")
                .update();
        if (updateTestCaseResponse.isSuccessful()) {
            ZephyrTestCase testCase = updateTestCaseResponse.getUpdatedTestCase();
            System.out.println("Test Case Name: " + testCase.getName());
            if (testCase.getSteps() != null) {
                System.out.println("Steps:");
                for (String step : testCase.getSteps()) {
                    System.out.println("- " + step);
                }
            }
        }  else {
            System.err.println("Failed to retrieve test case: " + updateTestCaseResponse.getErrorMessage());
        }

    }
}