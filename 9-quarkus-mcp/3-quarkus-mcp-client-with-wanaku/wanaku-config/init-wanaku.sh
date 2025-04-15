#!/bin/bash

echo "Configure telegram tool set the Auth Token"
wanaku targets tools configure --service=telegram --option=authorizationToken --value=$TELEGRAM_AUTHORIZATION_TOKEN
echo "Configure telegram tool set the Telegram ID of the default user"
wanaku targets tools configure --service=telegram  --option=telegramId --value=$TELEGRAM_CHAT_ID
echo "Start telegram tool"
wanaku tools add  --name telegram --uri "telegram://send-message" --description 'Sends a message to the user' --property 'message:string, the message to send to the user'  --required=message --type telegram


