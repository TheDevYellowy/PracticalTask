# Mini-games Plugin

### This project will not be maintained, you are free to fork and do whatever with it

## What is this?
This is a plugin for the latest version of minecraft 1.21.11 that adds functionality to create, join, and play different
mini-games that have been developed

## Why was it designed this way?
I wanted to have this project be easily expandable both inside this jar and also via other plugins. That's why there is
a [Game](https://github.com/TheDevYellowy/PracticalTask/tree/master/src/main/java/com/thedevyellowy/practicalTask/games/Game.java#L14) 
class. The different mini-games have to extend this so everything is organised and other classes can expand upon this if needed.

## One feature or improvement I decided not to add, and why I skipped it
One improvement I decided not to implement is a class for game management, I didn't add this because of the size of the plugin.
What I mean by that is that I thought that with the small amount of minigames that there are it would add more complexity than it would remove.

## Plugin expansion plans
If I were to expand upon this plugin I'd obviously finish the command handling for both minigames and maybe add a spleef 
implementation with resetting platforms by saving the block to be broken within the playable area and if it's air replace
it with the set block on minigame reset.