#!/bin/sh
set -eu

if [ "$#" -ne 4 ]; then
  echo "Usage: $0 <order-id> <payment-id> <amount> <currency>" >&2
  exit 1
fi

ORDER_ID="$1"
PAYMENT_ID="$2"
AMOUNT="$3"
CURRENCY="$4"
EVENT_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
PAID_AT=$(date -u +%Y-%m-%dT%H:%M:%SZ)
MESSAGE=$(printf '{"eventId":"%s","eventType":"PaymentConfirmed.v1","orderId":"%s","paymentId":"%s","paidAmount":"%s","currency":"%s","paidAt":"%s"}' \
  "$EVENT_ID" "$ORDER_ID" "$PAYMENT_ID" "$AMOUNT" "$CURRENCY" "$PAID_AT")

docker compose exec -T localstack awslocal sns publish \
  --topic-arn arn:aws:sns:us-east-1:000000000000:payment-events \
  --message "$MESSAGE" \
  --message-attributes '{"eventType":{"DataType":"String","StringValue":"PaymentConfirmed.v1"}}'
