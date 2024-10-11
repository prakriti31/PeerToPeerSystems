# Define variables
SRC_DIR = src/test
TARGET_DIR = target
JAVA_FILES = IndexingServerBenchmarkTests.java \
             IndexingServerPlot.java \
             PeerNodeBenchmarkTests.java \
             PeerNodePlot.java \
             PeerNetworkTestRequirement1.java \
             PeerNetworkTestRequirement2.java

# Default target
all: run-tests

# Target to run all Java test files
run-tests:
	@echo "Running all test files..."
	@for %%f in ($(JAVA_FILES)) do ( \
		set "classname=%%~nf" && \
		echo Running !classname!... && \
		java -cp "$(TARGET_DIR)\classes;$(TARGET_DIR)\lib\*;C:\path\to\junit-platform-console-standalone-<version>.jar;C:\path\to\junit-jupiter-api-<version>.jar;C:\path\to\junit-jupiter-engine-<version>.jar" org.junit.runner.JUnitCore $(SRC_DIR)/!classname! && \
		echo "---------------------------------------------------" \
	)

# Clean up (optional)
clean:
	@echo "Cleaning up..."
	del /Q $(TARGET_DIR)\*.class
