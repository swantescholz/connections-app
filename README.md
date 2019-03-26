# Connections - And Android App using libGDX/KTX, written in Kotlin

*Connections* is an Android game using physical rigid body physics (via Box2D) to represent human interactions.
I programmed it in Kotlin, using the lovely KTX extension of libGDX.
I hope this sourse code helps anybody who would like to program an Android game with Kotlin.

You can find the [app in the Google Play Store](https://play.google.com/store/apps/details?id=de.sscholz.connections).

## Credits

For my dear friend Simone :)
Lots of friendship and love to you!
And hugs! Many, many hugs!

Swante Scholz, 2019

*******

#### Used libraries:
- [libGDX](https://libgdx.badlogicgames.com/)
- [libKTX](https://libktx.github.io/)
- [Box2D Physics Engine](http://box2d.org/)

#### Music/Sounds:
- ["Boom Kick" by Goup_1](https://freesound.org/people/Goup_1/sounds/195396/)

#### Additional art:
- [Free Unicorn SVG Cut Files](https://shopcraftables.com/products/unicorn-free-svg-cut-file/)
- [Confined Particle, by Ryk](https://www.contextfreeart.org/gallery2/#design/2842)
- [Nonexistent Ambient Album Cover by untitled.bmp](https://www.contextfreeart.org/gallery2/#design/2834)
- "Bear side view silhouette", "Elephant Facing Right", "Duck Facing Right", "Snail Facing Right", designed by [Freepik from Flaticon](https://www.flaticon.com/authors/freepik)

To all my friends and family who playtested this game: Thank you!

## How to play

This game is about interpersonal connections, represented as physical interactions between objects.

Each person has various character traits that determine their physical behavior. Each connection type influences how two people interact with each other.

Part of the joy of this game is figuring out how these traits and connections work exactly.

Your goal is for each person to reach her given final destination (transparent), determined by the connections you draw between them.

For simplicity, the game just checks whether the connections you created are exactly those that it expects for the given level. So try to avoid connections that have effectively zero impact on the simulations. Otherwise the game might tell you that your solution is incorrect, even though all pieces align just right with their goal positions.

### Controls

Tap and hold on person: See details about that person
Tap person + release on another person:
Connect two people

On the top right you'll be able to select the type of connection you want to create.
On the bottom right you can select any connection you want to delete.
Other buttons let you play/pause the simulation, change the playback speed, reset the simulation,
and undo the last connection creation.
On the bottom left you see a timer, indicating the currrent time (in seconds) and the total simulation time that will be used for that level.