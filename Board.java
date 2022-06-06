import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Board {
    //colors related to the board
    Color bg_purple = new Color(130, 53, 245);
    Color bg_purple_stop = new Color(65, 26, 122);
    Color bg_grey1 = new Color(94, 80, 118);
    Color bg_grey2 = new Color(75, 64, 94);
    Color bg_black = new Color(18, 18, 18);

    //colors related to the letter boxes
    Color letter_empty = new Color(167, 113, 248);
    Color letter_wrong = new Color(155, 93, 247);
    Color letter_correct = new Color(46, 216, 60);
    Color letter_partial = new Color(214, 190, 0);

    //how close colors can be
    int sensitivity = 0;

    //classes needed
    Robot robot;
    DisplayMode mode;
    Dimension screenDim;
    Rectangle screen;
    Dimension boardDim;
    Rectangle board;

    //store where the location of each letter is
    int[][][][] letters;

    //constructor
    public Board() throws Exception {
        //get the entire screen
        robot = new Robot();
        mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        screenDim = new Dimension(mode.getWidth(), mode.getHeight());
        screen = new Rectangle(screenDim);

        //find where the board is located
        this.findBoard();

        //set up our letters array
        letters = new int[6][5][2][2];

        //find where the letters are
        this.findLetters();
    }

    //find the backspace button in the board
    public int[] findEnter(BufferedImage screenshot) {
        int screenHeight = screenDim.height;
        int screenWidth = screenDim.width;
        int[] anchor = new int[2];

        //look through entire screen starting from bottom left
        outerLoop: for (int i = 0; i < screenHeight; i ++) {
            for (int j = 0; j < screenWidth; j++) {
                //finds the bottom edge of the enter button
                if (isColor(screenshot, j, screenHeight - i - 1, bg_grey1) || 
                    isColor(screenshot, j, screenHeight - i - 1, bg_grey2)) {
                    anchor[0] = screenHeight - i - 1;
                    anchor[1] = j;
                    break outerLoop;
                }
            }
        }

        //get the top edge of the enter button
        while (isColor(screenshot, anchor[1], anchor[0], bg_grey1) || isColor(screenshot, anchor[1], anchor[0], bg_grey2))
            anchor[0]--;

        //return location of top edge of enter button
        return anchor;
    }

    //get the top left and bottom left corner of the board
    public void findBoard() {
        BufferedImage screenshot = captureScreen();
        int[] anchor = findEnter(screenshot);
        int[][] boardCorners = new int[2][2];
        //get edges
        int[] top = getEdge(screenshot, anchor[0], anchor[1], new int[]{-1, 0});
        int[] bottom = getEdge(screenshot, anchor[0], anchor[1], new int[]{1, 0});
        int[] left = getEdge(screenshot, anchor[0], anchor[1], new int[]{0, -1});
        int[] right = getEdge(screenshot, anchor[0], anchor[1], new int[]{0, 1});

        //calculate location of corners
        boardCorners[0][0] = top[0] + 1;
        boardCorners[0][1] = left[1] + 1;
        boardCorners[1][0] = bottom[0] - 1;
        boardCorners[1][1] = right[1] - 1;

        //get dimensions
        int boardHeight = boardCorners[1][0] - boardCorners[0][0];
        int boardWidth = boardCorners[1][1] - boardCorners[0][1];
        
        //create a Rectangle for the board
        boardDim = new Dimension(boardWidth, boardHeight);
        board = new Rectangle(boardCorners[0][1], boardCorners[0][0], boardDim.width, boardDim.height);
    }

    //function to help find each edge on the board
    public int[] getEdge(BufferedImage screenshot, int y, int x, int[] dir) {
        while (!isColor(screenshot, x, y, bg_black)) {
            y += dir[0];
            x += dir[1];
        }
        int[] edge = {y, x};
        return edge;
    }

    //find where each letter is
    public void findLetters() {
        BufferedImage board = this.captureBoard();
        int boardHeight = boardDim.height;
        int boardWidth = boardDim.width;
        int[] anchor = new int[2]; 

        //search from top left going left->right then top->down
        outerLoop: for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (isColor(board, j, i, letter_empty)) {
                    anchor[0] = i;
                    anchor[1] = j;
                    break outerLoop;
                }
            }
        }

        //find letter and gap heights
        int letterHeight = 0;
        int gapHeight = 0;
        while (isColor(board, anchor[1], anchor[0], letter_empty)) {
            anchor[0]++;
            letterHeight++;
        }
        while (!isColor(board, anchor[1], anchor[0], letter_empty)) {
            anchor[0]++;
            gapHeight++;
        }

        //calculate first component of the center
        int firstCenterHeight = anchor[0] - gapHeight - letterHeight / 2;

        //search from top left going top->down then left->right
        outerLoop: for (int j = 0; j < boardWidth; j++) {
            for (int i = 0; i < boardHeight; i++) {
                if (isColor(board, j, i, letter_empty)) {
                    anchor[0] = i;
                    anchor[1] = j;
                    break outerLoop;
                }
            }
        }

        //find letter and gap widths
        int letterWidth = 0;
        int gapWidth = 0;
        while (isColor(board, anchor[1], anchor[0], letter_empty)) {
            anchor[1]++;
            letterWidth++;
        }
        while (!isColor(board, anchor[1], anchor[0], letter_empty)) {
            anchor[1]++;
            gapWidth++;
        }

        //calculate second component of the center
        int firstCenterWidth = anchor[1] - gapWidth - letterWidth / 2;

        //calculate the centers of each letter box
        int centerHeight;
        int centerWidth;
        for (int m = 0; m < 6; m++) {
            for (int n = 0; n < 5; n++) {
                centerHeight = firstCenterHeight + m * (letterHeight + gapHeight);
                centerWidth = firstCenterWidth + n * (letterWidth + gapWidth);
                letters[m][n][0][0] = centerHeight - letterHeight / 2;
                letters[m][n][0][1] = centerWidth - letterWidth / 2;
                letters[m][n][1][0] = centerHeight + letterHeight / 2;
                letters[m][n][1][1] = centerWidth + letterWidth / 2;
            }
        }
    }

    //get the colors in each line
    public String getResult(int guess) throws Exception {
        BufferedImage boardImage = this.captureBoard();
        while (this.getResult(boardImage, guess, 0) == "-1") {
            if (guess > 0 && this.getResult(boardImage, guess-1, 0) == "-1")
                return "22222";
            else {
                Thread.sleep(100);
                boardImage = this.captureBoard();
            }
        }
        String result = "";
        String nextLetterResult = "";
        for (int i = 0; i < 5; i++) {
            nextLetterResult = getResult(boardImage, guess, i);
            // System.out.println(nextResult);
            result += nextLetterResult;
            // result += getResult(boardImage, guess, i);
        }
        return result;
    }

    //get the color for each letter box
    public String getResult(BufferedImage boardImage, int guess, int letter) {
        double diagRatio = 0.15;
        
        int topLeftY = letters[guess][letter][0][0];
        int topLeftX = letters[guess][letter][0][1];
        int bottomRightY = letters[guess][letter][1][0];
        int bottomRightX = letters[guess][letter][1][1];

        int locX = (int) (topLeftX + diagRatio * (bottomRightX - topLeftX));
        int locY = (int) (topLeftY + diagRatio * (bottomRightY - topLeftY));

        //wrong is 0
        if (isColor(boardImage, locX, locY, letter_wrong))
            return "0";
            
        //partial is 1
        if (isColor(boardImage, locX, locY, letter_partial))
            return "1";
            
        //correct is 2
        if (isColor(boardImage, locX, locY, letter_correct))
            return "2";
            
        //returns -1 upon error
        return "-1";
    }
    
    public boolean checkReset() {
        BufferedImage boardImage = this.captureBoard();
        return (this.getResult(boardImage, 0, 0).equals("-1"));
    }
    
    public boolean gameEnded() {
        BufferedImage boardImage = this.captureBoard();
        int locX = boardImage.getWidth() / 2;
        int locY = boardImage.getHeight() - 5;
        return isColor(boardImage, locX, locY, bg_purple_stop);
    }
    
    //check if something is a certain color
    public boolean isColor(BufferedImage image, int x, int y, Color c) {
        Color pixel = new Color(image.getRGB(x,y));
        if (!compareSensitivity(pixel.getRed(), c.getRed()))
            return false;
        if (!compareSensitivity(pixel.getGreen(), c.getGreen()))
            return false;
        if (!compareSensitivity(pixel.getBlue(), c.getBlue()))
            return false;
        return true;
    }

    //if you want to compare something that's just a tiny bit off
    public boolean compareSensitivity(int item, int target) {
        if (target - sensitivity <= item && item <= target + sensitivity)
            return true;
        return false;
    }

    //take a screen shot
    public BufferedImage captureScreen() {
        return robot.createScreenCapture(screen);
    }

    //take a picture only of where the board is
    public BufferedImage captureBoard() {
        return robot.createScreenCapture(board);
    }

    //get the dimensions of the board
    public Dimension getBoardDim() {
        return boardDim;
    }
}