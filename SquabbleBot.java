import java.lang.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import static java.awt.event.KeyEvent.*;

public class SquabbleBot {
    Robot robot;
    Board board;
    String[] guesses;
    String[] results;
    int guess_num;
    BufferedReader stdInput;
    
    public SquabbleBot() throws Exception { 
        robot = new Robot();
        board = new Board();
        guesses = new String[5];
        results = new String[5];
        for (int i = 0; i < 5; i++) {
            guesses[i] = "";
            results[i] = "";
        }
        guess_num = 0;
        stdInput = null;
    }

    public void play() throws Exception {
        boolean solved = false;
        boolean hasGameEnded = false;
        while (!hasGameEnded) {
            while(!board.checkReset())
                Thread.sleep(100);
            solved = false;
            for (int i = 0; i < 5; i++) {
                guesses[i] = nextGuess();
                enterGuess(guesses[i]);
                results[i] = getResult();
                if (results[i].equals("22222")) {
                    solved = true;
                    this.reset();
                    Thread.sleep(1000);
                    break;
                }
                guess_num += 1;
            }
            
            if (!solved) {
                String finalGuess = nextGuess();
                enterGuess(finalGuess);
                // String finalResult = getResult();
                this.reset();
                Thread.sleep(1000);
            }
            hasGameEnded = board.gameEnded();
        }
    }

    // have the robot enter a word
    public void enterGuess(String word) throws Exception {
        for (int i = 0; i < word.length(); i++) {
            robot.keyPress(keyCode(word.charAt(i)));
            Thread.sleep(10);
        }
        robot.keyPress(VK_ENTER);
        Thread.sleep(200);
    }

    // read what the result of the last guess was
    public String getResult() throws Exception {
        String lastResult = board.getResult(guess_num);
        return lastResult;
    }
    
    public String nextGuess() throws Exception {
        String gr = "";
        for (int i = 0; i < 5; i++)
            gr += guesses[i];
        for (int i = 0; i < 5; i++)
            gr += results[i];
        if (gr.equals(""))
           gr = "start";
        Process p = Runtime.getRuntime().exec("python Solver.py " + gr);
        stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String guess = stdInput.readLine();
        return guess;
    }
    
    public void reset() {
        for (int i = 0; i < 5; i++) {
            guesses[i] = "";
            results[i] = "";
        }
        guess_num = 0;
    }
    
    public int keyCode(char character) {
        switch (character) {
        case 'a': return VK_A;
        case 'b': return VK_B;
        case 'c': return VK_C;
        case 'd': return VK_D;
        case 'e': return VK_E;
        case 'f': return VK_F;
        case 'g': return VK_G;
        case 'h': return VK_H;
        case 'i': return VK_I;
        case 'j': return VK_J;
        case 'k': return VK_K;
        case 'l': return VK_L;
        case 'm': return VK_M;
        case 'n': return VK_N;
        case 'o': return VK_O;
        case 'p': return VK_P;
        case 'q': return VK_Q;
        case 'r': return VK_R;
        case 's': return VK_S;
        case 't': return VK_T;
        case 'u': return VK_U;
        case 'v': return VK_V;
        case 'w': return VK_W;
        case 'x': return VK_X;
        case 'y': return VK_Y;
        case 'z': return VK_Z;
        }
        return 0;
    }
    
    public void saveBoardImg(String filename) throws Exception {
        BufferedImage boardImg = board.captureBoard();
        File outputfile = new File(filename);
        ImageIO.write(boardImg, "png", outputfile);
    }
}