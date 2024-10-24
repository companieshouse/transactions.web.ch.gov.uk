#!/bin/bash

# Start script for transactions.web.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "transactions.web.ch.gov.uk.jar"
