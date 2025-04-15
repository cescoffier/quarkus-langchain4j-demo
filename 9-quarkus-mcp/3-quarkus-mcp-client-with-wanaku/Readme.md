# Install Wanaku locally


## Export your Telegram credentials

```
export TELEGRAM_AUTHORIZATION_TOKEN=<INSERT HERE YOUR TELEGRAM BOT AUTH TOKEN>
export TELEGRAM_CHAT_ID=<INSERT HERE YOUR TELEGRAM CHAT ID>
```

## Start Wanaku Router

Go to the Wanaku config folder

```
cd wanaku-config
docker-compose up -d
```

## Configure the Telegram Credentials and start Telegram Tool 

### Install Wanaku CLI

Get the CLI from https://github.com/wanaku-ai/wanaku/releases

### Configure and start Integram Wanaku Tool

NB: this is a shortcut to using Telegram for a demo. It basically sends a message to a specific user, instead of adding another tool to get ID for a specific user

```
cd wanaku-config
./init-wanaku.sh
```

If needed make sure that the file is executable
```
chmod +x init-wanaku.sh
```




