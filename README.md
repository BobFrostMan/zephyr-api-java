# Zephyr Java Client
This project provides a simple and convenient Java client for interacting with the Zephyr API.
It is designed to simplify the integration of your Java applications (mainly test automation frameworks) with Zephyr, allowing you to programmatically manage test cases, folders, statuses, priorities, and other resources.

## Motivation
I didn't find user-friendly not overloaded with internal entities and api logic client for simple Zephyr.
So it's an attempt to design simple client for Zephyr without additional headache. 

[Zephyr API documentation available here.](https://support.smartbear.com/zephyr-scale-cloud/api-docs/#section/Authentication/Generate-a-Key)

## Key Features
- Easy to Use: Intuitive API for integration into your projects.
- Support for Core Operations: Create, retrieve, and update test cases and folders.
- Data Caching: Local caching of statuses, priorities, folders, and the project to reduce the number of API requests.
- Fluent Interface: User-friendly way to create and update complex objects, such as test cases with steps.
- Extensibility: Easily add support for new Zephyr Scale API endpoints and entities.

## Terms of use
By using this project or its source code, for any purpose and in any shape or form, you grant your **implicit agreement** to all the following statements:

- You **condemn Russia and its military aggression against Ukraine**
- You **recognize that Russia is an occupant that unlawfully invaded a sovereign state**
- You **support Ukraine's territorial integrity, including its claims over temporarily occupied territories of Crimea and Donbas**
- You **reject false narratives perpetuated by Russian state propaganda**

Glory to Ukraine!

## Getting Started

### Prerequisites
Java 11 or later.

### Installation
This project is currently under development, so there is no official release on Maven Central yet. You can clone the repository and build the JAR file yourself:

```bash
git clone <your repository link>
cd zephyr-scale-java-client
mvn clean install
```
After a successful build, add the dependency to your project's pom.xml (if you are using Maven):

```maven
<dependency>
    <groupId>io.github.bobfrostman</groupId>
    <artifactId>zephyr-scale-java-client</artifactId>
    <version>1.0.0</version>
</dependency>
```
Or in your build.gradle (if you are using Gradle):
```gradle
implementation 'io.github.bobfrostman:zephyr-scale-java-client:DEVELOPMENT-SNAPSHOT' // Replace with the actual version
```

## Client Initialization
To start working with the Zephyr  API, you need to create an instance of ZephyrProjectApiClient using the Builder:

```java
import io.github.bobfrostman.zephyr.client.IZephyrProjectApiClient;
import io.github.bobfrostman.zephyr.client.ZephyrProjectApiClientBuilder;

public class Example {

    public static void main(String[] args) {
        String apiUrl = "YOUR_ZEPHYR_SCALE_API_URL";
        String token = "YOUR_ZEPHYR_SCALE_API_TOKEN";
        String projectKey = "YOUR_PROJECT_KEY";
        boolean verbose = true; // Enable detailed logging (optional)
    
        IZephyrProjectApiClient client = new ZephyrProjectApiClientBuilder()
                .withApiUrl(apiUrl)
                .withToken(token)
                .withProjectKey(projectKey)
                .withVerbose(verbose)
                .build();

        // You can now use 'client' to interact with the API
    }
}
```
## Usage Examples
### Getting Test Case Statuses
```java
import io.github.bobfrostman.zephyr.client.response.GetStatusesResponse;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCaseStatus;

// ... (client initialization) ...

    GetStatusesResponse statusesResponse = client.getStatuses();
    if (statusesResponse.isSuccessful()) {
        for (ZephyrTestCaseStatus status : statusesResponse.getStatuses()) {
            System.out.println(status.getName() + " (ID: " + status.getId() + ")");
        }
    } else {
        System.err.println("Failed to retrieve statuses: " + statusesResponse.getErrorMessage());
    }
```

### Creating a New Test Case with Steps
```java
import io.github.bobfrostman.zephyr.client.response.CreateTestCaseResponse;

    // ... (client initialization) ...
    
    CreateTestCaseResponse createResponse = client.newTestCase("YOUR_PROJECT_KEY")
        .withName("My New Test Case")
        .withObjective("Verify important functionality")
        .withPrecondition("Environment needs to be set up")
        .withStep("Step 1: Perform action X")
        .withStep("Step 2: Verify that result Y is as expected")
        .create();
        
    if (createResponse.isSuccessful()) {
        System.out.println("Test case created successfully with ID: " + createResponse.getCreatedTestCase().getKey());
    } else {
        System.err.println("Failed to create test case: " + createResponse.getErrorMessage());
    }
```

### Getting a Test Case by Key with Steps
```java
import io.github.bobfrostman.zephyr.client.response.GetTestCaseResponse;
import io.github.bobfrostman.zephyr.entity.ZephyrTestCase;
    
    // ... (client initialization) ...
    
    String testCaseKey = "YOUR_TEST_CASE_KEY";
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
```

### Creating a New Folder
```java
import io.github.bobfrostman.zephyr.client.response.CreateFolderResponse;

// ... (client initialization) ...

    CreateFolderResponse createFolderResponse = client.newTestCaseFolder()
        .withName("New Test Case Folder")
        .create();

    if (createFolderResponse.isSuccessful()) {
        System.out.println("Folder created successfully with ID: " + createFolderResponse.getCreatedFolder().getId());
    } else {
        System.err.println("Failed to create folder: " + createFolderResponse.getErrorMessage());
    }
```
## Future Plans
- Support for other Zephyr Scale API endpoints (test executions, reports, etc.).
- Additional client configuration options (timeouts, retry policy).
- More flexible cache management.
- Javadoc documentation.

## Contributing
Contributions to the project are welcome! You can help by reporting bugs, suggesting new features, or submitting pull requests with your changes.

## License
MIT License

## Feedback
I'm glad to hear any feedback from you via [Facebook](https://www.facebook.com/maksym.zaverukha.37) or Telegram (@foggger)
Special thanks to [Tyrrz](https://github.com/Tyrrrz) for Terms of use.

Thank you for using the Zephyr Scale Java Client!