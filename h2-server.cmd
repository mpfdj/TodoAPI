@echo on

java -cp C:\Users\TO11RC\.m2\repository\com\h2database\h2\2.4.240\h2-2.4.240.jar org.h2.tools.Server ^
    -tcp ^
    -web ^
    -tcpAllowOthers ^
    -tcpPort 9092 ^
    -webPort 9093 ^
    -baseDir "C:\Users\TO11RC\OneDrive - ING\miel\workspace\Java\TodoAPI\h2"
