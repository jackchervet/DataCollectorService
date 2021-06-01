# DataCollectorService

Lambda function for retrieving recent Match Ids from SMITE API.

## Contract

- Downstream SQS: MatchIdQueue
- Runs on CRON schedule every 10 min

## Building

Run `sbt assembly` to construct far jar and manually upload to MatchIdService lambda.