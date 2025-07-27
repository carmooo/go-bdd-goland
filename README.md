# Paddle Go-BDD GoLand Plugin

A GoLand/IntelliJ plugin that provides seamless integration for BDD testing in Go projects using go-bdd.

## Features

- **Run individual scenarios** from `.feature` files directly in the IDE
- **Run entire features** with all scenarios
- **Support for scenario names** including URLs and paths with forward slashes
- **Dynamic test function discovery** from your test files
- **Case-insensitive tag matching** for flexible test organization

## Requirements

- GoLand 2024.3+ or IntelliJ IDEA Ultimate with Go plugin
- Go project using go-bdd framework

## Project Structure

Your Go project must have the following structure to be detected by the plugin:

```
your-project/
├── features/           # Gherkin feature files
│   └── *.feature
└── bdd_test.go        # Required: BDD test file
```

### Dependencies
The plugin detects go-bdd projects by looking for imports in your `bdd_test.go` file:

```go
import "github.com/PaddleHQ/go-bdd/v1"  // or any version (v1, v2, etc.)
```

### Test Function Naming
Your test functions in `bdd_test.go` should be named to match your scenario tags:

```go
func TestHTTPFeatures(t *testing.T) {
    // BDD test implementation
}

func TestGRPCFeatures(t *testing.T) {
    // BDD test implementation  
}
```

**Note**: If you have only one test function, all scenarios will automatically use that function. With multiple test functions, all scenarios must have matching tags.

For single test function projects, use:
```go
func TestFeatures(t *testing.T) {
    // BDD test implementation
}
```

The plugin will automatically discover these test functions and match them with tags in your feature files.

## Usage

### Running Tests

1. **Run a single scenario**: Click the green play button next to any scenario in a `.feature` file
2. **Run an entire feature**: Click the green play button next to the feature name

### Scenario Tags
Tag your scenarios to specify which test function should execute them:

```gherkin
@HTTP
Scenario: User login via REST API
  Given I have valid credentials
  When I send a POST request to "/login"
  Then I should receive a 200 status code

@GRPC  
Scenario: User login via gRPC
  Given I have valid credentials
  When I call the Login gRPC method
  Then I should receive a successful response
```

## Installation

1. Download the latest release zip file from the GitHub releases page
2. In GoLand/IntelliJ, go to Settings → Plugins → Install Plugin from Disk
3. Select the downloaded zip file
4. Restart the IDE

## Configuration

### Automatic Detection
The plugin automatically detects go-bdd projects when:
- A `bdd_test.go` file exists in your project
- The file imports `github.com/PaddleHQ/go-bdd/v*`
- Test functions are properly defined

### Tag Matching
- Tags are matched case-insensitively (`@http` matches `TestHTTPFeatures`)
- If no tags match available test functions, the plugin will not create run configurations for multi-function projects
- For single-function projects, the plugin uses the available function regardless of tags

## Technical Details

### Go Test Integration
The plugin generates Go test patterns that work with `go test -run` flag, handling complex regex parsing rules for scenario names with special characters.

### Pattern Generation
- Uses `\Q...\E` literal escaping for scenario names
- Supports top-level alternation for multiple test functions
- Handles Go test's `splitRegexp` behavior correctly

## Troubleshooting

### Plugin Not Working / Tests Not Running
- Ensure your project has a `bdd_test.go` file
- Verify the go-bdd import is present and correctly formatted
- Check that test functions follow the naming convention
- Verify scenario tags match your test function names (case-insensitive)
- For scenarios without tags, ensure you have only one test function defined

## TODO

1. Navigate from feature step to implementation
2. Generate implementation func for step if there is none
3. Right click on test function and run with tag