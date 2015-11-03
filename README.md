# SkypeToTelegramBot
Skype &lt;-> Telegram Bot, finally, no more skype client...

## Downloads
Grab the latest build from the CI here [![Build Status](http://ci.zackpollard.pro/job/SkypeToTelegramBot/badge/icon)](http://ci.zackpollard.pro/job/SkypeToTelegramBot/)

## Usage
java -jar SkypeToTelegramBot.jar TG_BOT_API_KEY

Create a group chat with just you and the bot in it after successfully logging in and use the /link command.

### Commands
#### Private Chat
/login [username] [password]   
/logout

#### Group Chat
/link - Currently the only way to link chats and requires a message to have been sent in the chat you want since you logged in.   
/unlink
