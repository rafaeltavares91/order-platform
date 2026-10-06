#!/bin/sh
set -eu

PAYMENT_REQUESTED_TOPIC_ARN="arn:aws:sns:us-east-1:000000000000:payment-requested"
PAYMENT_CONFIRMED_TOPIC_ARN="arn:aws:sns:us-east-1:000000000000:payment-confirmed"

awslocal sns create-topic --name payment-requested >/dev/null
awslocal sns create-topic --name payment-confirmed >/dev/null

awslocal sqs create-queue --queue-name payment-requested-dlq >/dev/null
awslocal sqs create-queue --queue-name payment-confirmed-dlq >/dev/null
awslocal sqs create-queue --queue-name payment-requested >/dev/null
awslocal sqs create-queue --queue-name payment-confirmed >/dev/null

REQUEST_QUEUE_URL="http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/payment-requested"
CONFIRMED_QUEUE_URL="http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/payment-confirmed"
REQUEST_QUEUE_ARN="arn:aws:sqs:us-east-1:000000000000:payment-requested"
CONFIRMED_QUEUE_ARN="arn:aws:sqs:us-east-1:000000000000:payment-confirmed"
REQUEST_DLQ_ARN="arn:aws:sqs:us-east-1:000000000000:payment-requested-dlq"
CONFIRMED_DLQ_ARN="arn:aws:sqs:us-east-1:000000000000:payment-confirmed-dlq"

queue_attributes() {
  dlq_arn=$1
  topic_arn=$2
  queue_arn=$3
  printf '{"RedrivePolicy":"{\\"deadLetterTargetArn\\":\\"%s\\",\\"maxReceiveCount\\":\\"5\\"}","Policy":"{\\"Version\\":\\"2012-10-17\\",\\"Statement\\":[{\\"Effect\\":\\"Allow\\",\\"Principal\\":{\\"Service\\":\\"sns.amazonaws.com\\"},\\"Action\\":\\"sqs:SendMessage\\",\\"Resource\\":\\"%s\\",\\"Condition\\":{\\"ArnEquals\\":{\\"aws:SourceArn\\":\\"%s\\"}}}]}"}' \
    "$dlq_arn" "$queue_arn" "$topic_arn"
}

REQUEST_ATTRIBUTES=$(queue_attributes "$REQUEST_DLQ_ARN" "$PAYMENT_REQUESTED_TOPIC_ARN" "$REQUEST_QUEUE_ARN")
CONFIRMED_ATTRIBUTES=$(queue_attributes "$CONFIRMED_DLQ_ARN" "$PAYMENT_CONFIRMED_TOPIC_ARN" "$CONFIRMED_QUEUE_ARN")

awslocal sqs set-queue-attributes --queue-url "$REQUEST_QUEUE_URL" --attributes "$REQUEST_ATTRIBUTES"
awslocal sqs set-queue-attributes --queue-url "$CONFIRMED_QUEUE_URL" --attributes "$CONFIRMED_ATTRIBUTES"

REQUEST_SUBSCRIPTION_ARN=$(awslocal sns subscribe --topic-arn "$PAYMENT_REQUESTED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$REQUEST_QUEUE_ARN" --query SubscriptionArn --output text)
CONFIRMED_SUBSCRIPTION_ARN=$(awslocal sns subscribe --topic-arn "$PAYMENT_CONFIRMED_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$CONFIRMED_QUEUE_ARN" --query SubscriptionArn --output text)

awslocal sns set-subscription-attributes --subscription-arn "$REQUEST_SUBSCRIPTION_ARN" \
  --attribute-name RawMessageDelivery --attribute-value true
awslocal sns set-subscription-attributes --subscription-arn "$REQUEST_SUBSCRIPTION_ARN" \
  --attribute-name FilterPolicy --attribute-value '{"eventType":["PaymentRequested.v1"]}'
awslocal sns set-subscription-attributes --subscription-arn "$CONFIRMED_SUBSCRIPTION_ARN" \
  --attribute-name RawMessageDelivery --attribute-value true
awslocal sns set-subscription-attributes --subscription-arn "$CONFIRMED_SUBSCRIPTION_ARN" \
  --attribute-name FilterPolicy --attribute-value '{"eventType":["PaymentConfirmed.v1"]}'
