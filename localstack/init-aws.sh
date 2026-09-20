#!/bin/sh
set -eu

ORDER_TOPIC_ARN="arn:aws:sns:us-east-1:000000000000:order-events"
PAYMENT_TOPIC_ARN="arn:aws:sns:us-east-1:000000000000:payment-events"

awslocal sns create-topic --name order-events >/dev/null
awslocal sns create-topic --name payment-events >/dev/null

awslocal sqs create-queue --queue-name order-payment-requested-dlq >/dev/null
awslocal sqs create-queue --queue-name order-payment-confirmed-dlq >/dev/null
awslocal sqs create-queue --queue-name order-payment-requested >/dev/null
awslocal sqs create-queue --queue-name order-payment-confirmed >/dev/null

REQUEST_QUEUE_URL="http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-payment-requested"
CONFIRMED_QUEUE_URL="http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/order-payment-confirmed"
REQUEST_QUEUE_ARN="arn:aws:sqs:us-east-1:000000000000:order-payment-requested"
CONFIRMED_QUEUE_ARN="arn:aws:sqs:us-east-1:000000000000:order-payment-confirmed"
REQUEST_DLQ_ARN="arn:aws:sqs:us-east-1:000000000000:order-payment-requested-dlq"
CONFIRMED_DLQ_ARN="arn:aws:sqs:us-east-1:000000000000:order-payment-confirmed-dlq"

REQUEST_ATTRIBUTES=$(jq -cn \
  --arg dlq "$REQUEST_DLQ_ARN" --arg topic "$ORDER_TOPIC_ARN" --arg queue "$REQUEST_QUEUE_ARN" \
  '{RedrivePolicy:({deadLetterTargetArn:$dlq,maxReceiveCount:"5"}|tojson),Policy:({Version:"2012-10-17",Statement:[{Effect:"Allow",Principal:{Service:"sns.amazonaws.com"},Action:"sqs:SendMessage",Resource:$queue,Condition:{ArnEquals:{"aws:SourceArn":$topic}}}]}|tojson)}')
CONFIRMED_ATTRIBUTES=$(jq -cn \
  --arg dlq "$CONFIRMED_DLQ_ARN" --arg topic "$PAYMENT_TOPIC_ARN" --arg queue "$CONFIRMED_QUEUE_ARN" \
  '{RedrivePolicy:({deadLetterTargetArn:$dlq,maxReceiveCount:"5"}|tojson),Policy:({Version:"2012-10-17",Statement:[{Effect:"Allow",Principal:{Service:"sns.amazonaws.com"},Action:"sqs:SendMessage",Resource:$queue,Condition:{ArnEquals:{"aws:SourceArn":$topic}}}]}|tojson)}')

awslocal sqs set-queue-attributes --queue-url "$REQUEST_QUEUE_URL" --attributes "$REQUEST_ATTRIBUTES"
awslocal sqs set-queue-attributes --queue-url "$CONFIRMED_QUEUE_URL" --attributes "$CONFIRMED_ATTRIBUTES"

REQUEST_SUBSCRIPTION_ARN=$(awslocal sns subscribe --topic-arn "$ORDER_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$REQUEST_QUEUE_ARN" --query SubscriptionArn --output text)
CONFIRMED_SUBSCRIPTION_ARN=$(awslocal sns subscribe --topic-arn "$PAYMENT_TOPIC_ARN" --protocol sqs \
  --notification-endpoint "$CONFIRMED_QUEUE_ARN" --query SubscriptionArn --output text)

awslocal sns set-subscription-attributes --subscription-arn "$REQUEST_SUBSCRIPTION_ARN" \
  --attribute-name RawMessageDelivery --attribute-value true
awslocal sns set-subscription-attributes --subscription-arn "$REQUEST_SUBSCRIPTION_ARN" \
  --attribute-name FilterPolicy --attribute-value '{"eventType":["PaymentRequested.v1"]}'
awslocal sns set-subscription-attributes --subscription-arn "$CONFIRMED_SUBSCRIPTION_ARN" \
  --attribute-name RawMessageDelivery --attribute-value true
awslocal sns set-subscription-attributes --subscription-arn "$CONFIRMED_SUBSCRIPTION_ARN" \
  --attribute-name FilterPolicy --attribute-value '{"eventType":["PaymentConfirmed.v1"]}'
