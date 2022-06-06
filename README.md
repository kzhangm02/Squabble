# Squabble
A program to play Squabble, a real-time multiplayer game based on Wordle.
https://squabble.me/

Run the main method in `Main.java` and open a Squabble game in a new window. The Squabble board must be entirely visible on the screen.

### Algorithm

The program reads the Squabble board from the visible part of the screen. It then makes a guess using the algorithm in `Solver.py`. The AI calculates a score for each possible word and guesses the highest scoring word. The score is based on a weighted sum of the agent's propensity to exploit (use available knowledge) and explore (seek new information). Initially, the propensity to exploit is zero and increases exponentially with each guess.

### Performance

The bot achieves super-human performance. On average, the bot guesses the correct word in 3.5 guesses, taking less than 3 seconds.
