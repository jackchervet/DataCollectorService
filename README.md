# DataCollectorService

Lambda function for retrieving sampling of recent Match data from SMITE API.

## Contract

- Downstream DDB: match-data-raw
- Runs on CRON schedule every 60 min

## Building

Run `sbt assembly` to construct far jar and manually upload to MatchIdService lambda.