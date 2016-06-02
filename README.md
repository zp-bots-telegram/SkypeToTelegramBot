# SkypeToTelegramBot   
Skype &lt;-> Telegram Bot, finally, no more skype client...

## Downloads   
Grab the latest build from the CI here [![Build Status](http://ci.zackpollard.pro/job/SkypeToTelegramBot/badge/icon)](http://ci.zackpollard.pro/job/SkypeToTelegramBot/)

## Usage
Create a bot using the telegram BotFather   
Give this bot access to read all messages in group chats with the /setprivacy command in BotFather

Start the bot using java -jar SkypeToTelegramBot.jar TG_BOT_API_KEY

Create a group chat with just you and the bot in it after successfully logging in and use the /link command.

### Commands
#### Telegram Private Chat With Bot   
/login [username] [password]   
/logout

#### Telegram Group Chat   
/link - Shows a selection of clickable buttons of the known group chats.   
/link (chatID) - You can get the chat ID of a skype chat by typing /showname in the skype chat.   
/link (username) - You can link to a skype private chat by just entering the username of the person you want to link to.   
/unlink - Removes the link to the skype chat if one exists.   
