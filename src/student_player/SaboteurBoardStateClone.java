package student_player;

import java.util.ArrayList;
import java.util.Random;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import boardgame.Board;

public class SaboteurBoardStateClone {

	public static final int BOARD_SIZE = 14;
    public static final int originPos = 5;

    public static final int EMPTY = -1;
    public static final int TUNNEL = 1;
    public static final int WALL = 0;

    private static int FIRST_PLAYER = 1;

    private SaboteurTile[][] board;
    private int[][] intBoard;
    //player variables:
    // Note: Player 1 is active when turnplayer is 1;
    private ArrayList<SaboteurCard> player1Cards; //hand of player 1
    private ArrayList<SaboteurCard> player2Cards; //hand of player 2
    private int player1nbMalus;
    private int player2nbMalus;
    private boolean[] player1hiddenRevealed = {false,false,false};
    private boolean[] player2hiddenRevealed = {false,false,false};

    private ArrayList<SaboteurCard> Deck; //deck form which player pick
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
    protected SaboteurTile[] hiddenCards = new SaboteurTile[3];
    private boolean[] hiddenRevealed = {false,false,false}; //whether hidden at pos1 is revealed, hidden at pos2 is revealed, hidden at pos3 is revealed.

    private int turnPlayer;
    private int turnNumber;
    private int winner;
    private Random rand;
    //private SaboteurBoardState boardState;
	
	public SaboteurBoardStateClone(SaboteurBoardState boardState) {
		this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
		
		SaboteurTile[][] clonedBoard = boardState.getHiddenBoard();
		int[][] clonedIntBoard = boardState.getHiddenIntBoard();
		
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = clonedBoard[i][j];
            }
        }
        this.intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                this.intBoard[i][j] = clonedIntBoard[i][j];
            }
        }
        
        for(int i=0;i<hiddenCards.length;i++) {
        	hiddenCards[i] = new SaboteurTile("8");
        }
        
        this.Deck = SaboteurCard.getDeck();
        //initialize the player effects:
        player1nbMalus = 0;
        player2nbMalus = 0;
        
        //initialize the players hands:
        if(boardState.getTurnPlayer() == 1) {
			this.player1Cards = boardState.getCurrentPlayerCards();
			
			ArrayList<SaboteurCard> deck = SaboteurCard.getDeck();
			for (SaboteurCard card : player1Cards) {
				deck.remove(card);
			}
			// opponent's cards are the rest of the deck
			this.player2Cards = deck;
			
		}
		else {
			this.player2Cards = boardState.getCurrentPlayerCards();
			ArrayList<SaboteurCard> deck = SaboteurCard.getDeck();
			for (SaboteurCard card : player2Cards) {
				deck.remove(card);
			}
			// opponent's cards are the rest of the deck
			this.player1Cards = deck;
		}
        winner = Board.NOBODY;
        turnPlayer = boardState.getTurnPlayer();
        turnNumber = 0;
	}
	
	public SaboteurBoardStateClone(SaboteurBoardStateClone boardState) {
		this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
		
		SaboteurTile[][] clonedBoard = boardState.getUpdatedHiddenBoard();
		int[][] clonedIntBoard = boardState.getUpdatedHiddenIntBoard();
		
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = clonedBoard[i][j];
            }
        }
        this.intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                this.intBoard[i][j] = clonedIntBoard[i][j];
            }
        }
        
        for(int i=0;i<hiddenCards.length;i++) {
        	hiddenCards[i] = new SaboteurTile("8");
        }
        
        this.Deck = SaboteurCard.getDeck();
        //initialize the player effects:
        player1nbMalus = 0;
        player2nbMalus = 0;
        
        //initialize the players hands:
        if(boardState.getTurnPlayer() == 1) {
			this.player1Cards = boardState.getUpdatedCurrentPlayerCards();
			
			ArrayList<SaboteurCard> deck = SaboteurCard.getDeck();
			for (SaboteurCard card : player1Cards) {
				deck.remove(card);
			}
			// opponent's cards are the rest of the deck
			this.player2Cards = deck;
			
		}
		else {
			this.player2Cards = boardState.getUpdatedCurrentPlayerCards();
			ArrayList<SaboteurCard> deck = SaboteurCard.getDeck();
			for (SaboteurCard card : player2Cards) {
				deck.remove(card);
			}
			// opponent's cards are the rest of the deck
			this.player1Cards = deck;
		}
        winner = Board.NOBODY;
        turnPlayer = boardState.getTurnPlayer();
        turnNumber = 0;
	}

    public SaboteurTile[][] getBoardForDisplay(){
        //gives the board adapted for display, i.e hidden cards are changed to SaboteurTile("goalTile") while they have not been revealed to the player which turn it is!
        SaboteurTile[][] outboard = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boolean isAnHiddenPos =false;
                for(int h=0;h<3;h++){
                    if(hiddenPos[h][0] == i && hiddenPos[h][1] ==j){
                        if(this.player1hiddenRevealed[h] && turnPlayer==1 || this.player2hiddenRevealed[h] && turnPlayer==0 ){
                            outboard[i][j] = this.board[i][j] ;
                        }
                        else{
                            outboard[i][j] = new SaboteurTile("goalTile");
                        }
                        isAnHiddenPos =true;
                        break;
                    }
                }
                if(!isAnHiddenPos)  outboard[i][j] = this.board[i][j] ;
            }
        }
        return outboard;
    }


    public int getNbMalus(int playerNb){
        if(playerNb==1) return this.player1nbMalus;
        return this.player2nbMalus;
    }

    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int i=0;i<3;i++) {
            if (!hiddenRevealed[i]){
                objHiddenList.add(this.board[hiddenPos[i][0]][hiddenPos[i][1]]);
            }
        }
        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2] ) return false;
                else if(path[0][0] == 0 && path[0][1]== 0 && path[0][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify right side
        if(pos[1]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] + 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2]) return false;
                else if(path[2][0] == 0 && path[2][1]== 0 && path[2][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify upper side
        if(pos[0]>0) {
            SaboteurTile neighborCard = this.board[pos[0]-1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][2],path[1][2],path[2][2]};
                int[] np={neighborPath[0][0],neighborPath[1][0],neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify bottom side:
        if(pos[0]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]+1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][0],path[1][0],path[2][0]};
                int[] np={neighborPath[0][2],neighborPath[1][2],neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }
    
    
    public ArrayList<int[]> possiblePositions(SaboteurTile card) {
        // Given a card, returns all the possiblePositions at which the card could be positioned in an ArrayList of int[];
        // Note that the card will not be flipped in this test, a test for the flipped card should be made by giving to the function the flipped card.
    	ArrayList<int[]> possiblePos = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < BOARD_SIZE) {
                            if (this.verifyLegit(card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                possiblePos.add(new int[]{i + moves[m][0], j +moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return possiblePos;
    }

    public ArrayList<SaboteurMove> getAllLegalMoves() {
    	ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == 1){
            hand = this.player1Cards;
            isBlocked= player1nbMalus > 0;
        }
        else {
        	
        	//TODO: fix this later, getdeck()-player1cards-board cards
            hand = this.player2Cards;
            isBlocked= player2nbMalus > 0;
        }

        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        for(SaboteurCard card : hand){
            if( card instanceof SaboteurTile && !isBlocked) {
                ArrayList<int[]> allowedPositions = possiblePositions((SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1],turnPlayer));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = possiblePositions(flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1],turnPlayer));
                    }
                }
            }
            else if(card instanceof SaboteurBonus){
                if(turnPlayer ==1){
                    if(player1nbMalus > 0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
                }
                else if(player2nbMalus>0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMap){
                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
                    if(! this.hiddenRevealed[i]) legalMoves.add(new SaboteurMove(card,hiddenPos[i][0],hiddenPos[i][1],turnPlayer));
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=originPos || j!= originPos) && (i != hiddenPos[0][0] || j!=hiddenPos[0][1] )
                           && (i != hiddenPos[1][0] || j!=hiddenPos[1][1] ) && (i != hiddenPos[2][0] || j!=hiddenPos[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j,turnPlayer));
                        }
                    }
                }
            }
        }
        // we can also drop any of the card in our hand
        for(int i=0;i<hand.size();i++) {
            legalMoves.add(new SaboteurMove(new SaboteurDrop(), i, 0, turnPlayer));
        }
        return legalMoves;
    }

    public boolean isLegal(SaboteurMove m) {
        // For a move to be legal, the player must have the card in its hand
        // and then the game rules apply.
        // Note that we do not test the flipped version. To test it: use the flipped card in the SaboteurMove object.
    	SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();
        int currentPlayer = m.getPlayerID();
        if (currentPlayer != turnPlayer) return false;

        ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == 1){
            hand = this.player1Cards;
            isBlocked= player1nbMalus > 0;
        }
        else {
        	
            hand = this.player2Cards;
            isBlocked= player2nbMalus > 0;
        }
        if(testCard instanceof SaboteurDrop){
            if(hand.size()>=pos[0]){
                return true;
            }
        }
        boolean legal = false;
        for(SaboteurCard card : hand){
            if (card instanceof SaboteurTile && testCard instanceof SaboteurTile && !isBlocked) {
                if(((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())){
                    return verifyLegit(((SaboteurTile) card).getPath(),pos);
                }
                else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())){
                    return verifyLegit(((SaboteurTile) card).getFlipped().getPath(),pos);
                }
            }
            else if (card instanceof SaboteurBonus && testCard instanceof SaboteurBonus) {
                if (turnPlayer == 1) {
                    if (player1nbMalus > 0) return true;
                } else if (player2nbMalus > 0) return true;
                return false;
            }
            else if (card instanceof SaboteurMalus && testCard instanceof SaboteurMalus ) {
                return true;
            }
            else if (card instanceof SaboteurMap && testCard instanceof SaboteurMap) {
                int ph = 0;
                for(int j=0;j<3;j++) {
                    if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                }
                if (!this.hiddenRevealed[ph])
                    return true;
            }
            else if (card instanceof SaboteurDestroy && testCard instanceof SaboteurDestroy) {
                int i = pos[0];
                int j = pos[1];
                if (this.board[i][j] != null && (i != originPos || j != originPos) && (i != hiddenPos[0][0] || j != hiddenPos[0][1])
                        && (i != hiddenPos[1][0] || j != hiddenPos[1][1]) && (i != hiddenPos[2][0] || j != hiddenPos[2][1])) {
                    return true;
                }
            }
        }
        return legal;
    }
    
    public void processMove(SaboteurMove m) throws IllegalArgumentException {
    	
    	if (!isLegal(m)) {
            throw new IllegalArgumentException("Invalid move. Move: " + m.toPrettyString());
        }

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();

        if(testCard instanceof SaboteurTile){
            this.board[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) testCard).getIdx());
            if(turnPlayer==1){
                //Remove from the player card the card that was used.
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player1Cards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player1Cards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
            else {
                for (SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player2Cards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player2Cards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurBonus){
            if(turnPlayer==1){
                player1nbMalus --;
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurBonus) {
                        this.player1Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                player2nbMalus --;
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurBonus) {
                        this.player2Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMalus){
            if(turnPlayer==1){
                player2nbMalus ++;
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurMalus) {
                        this.player1Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                player1nbMalus ++;
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurMalus) {
                        this.player2Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMap){
            if(turnPlayer==1){
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurMap) {
                        this.player1Cards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                        }
                        this.player1hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurMap) {
                        this.player2Cards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                        }
                        this.player2hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if (testCard instanceof SaboteurDestroy) {
            int i = pos[0];
            int j = pos[1];
            if(turnPlayer==1){
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurDestroy) {
                        this.player1Cards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
            else {
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurDestroy) {
                        this.player2Cards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurDrop){
            if(turnPlayer==1) this.player1Cards.remove(pos[0]);
            else this.player2Cards.remove(pos[0]);
        }
//        this.draw();
//        this.updateWinner();
        turnPlayer = 1 - turnPlayer; // Swap player
        turnNumber++;
    }

    public void printBoard() {
        System.out.println(this.toString());
    }
    
    public boolean gameOver() {
        return this.Deck.size()==0 && this.player1Cards.size()==0 && this.player2Cards.size()==0 || winner != Board.NOBODY;
    }

    public SaboteurMove getRandomMove() {
        ArrayList<SaboteurMove> moves = getAllLegalMoves();
        return moves.get(rand.nextInt(moves.size()));
    }
    
    public int getTurnPlayer() {
        if(turnNumber==0){ //at first, the board is playing
            return Board.BOARD;
        }
        return turnPlayer;
    }
    
    
    public ArrayList<SaboteurCard> getUpdatedCurrentPlayerCards(){
        if(turnPlayer==1){
            ArrayList<SaboteurCard> p1Cards = new ArrayList<SaboteurCard>();
            for(int i=0;i<this.player1Cards.size();i++){
                p1Cards.add(i,SaboteurCard.copyACard(this.player1Cards.get(i).getName()));
            }
            return p1Cards;
        }
        else{
            ArrayList<SaboteurCard> p2Cards = new ArrayList<SaboteurCard>();
            for(int i=0;i<this.player2Cards.size();i++){
                p2Cards.add(i,SaboteurCard.copyACard(this.player2Cards.get(i).getName()));
            }
            return p2Cards;
        }
    }
    
    public int[][] getUpdatedHiddenIntBoard() {
        //update the int board, and provide it to the player with the hidden objectives set at EMPTY.
        //Note that this function is available to the player.
        boolean[] listHiddenRevealed;
        if(turnPlayer==1) listHiddenRevealed= player1hiddenRevealed;
        else listHiddenRevealed = player2hiddenRevealed;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = EMPTY;
                        }
                    }
                }
                else {
                    boolean isAnHiddenObjective = false;
                    for(int h=0;h<3;h++) {
                        if(this.board[i][j].getIdx().equals(this.hiddenCards[h].getIdx())){
                            if(!listHiddenRevealed[h]){
                                isAnHiddenObjective = true;
                            }
                            break;
                        }
                    }
                    if(!isAnHiddenObjective) {
                        int[][] path = this.board[i][j].getPath();
                        for (int k = 0; i < 3; i++) {
                            for (int h = 0; i < 3; i++) {
                                this.intBoard[i * 3 + k][j * 3 + h] = path[h][2-k];
                            }
                        }
                    }
                }
            }
        }

        return this.intBoard; }
    
    public SaboteurTile[][] getUpdatedHiddenBoard(){
        // returns the board in SaboteurTile format, where the objectives become the 8 tiles.
        // Note the inconsistency with the getHiddenIntBoard where the objectives become only -1
        // this is to stress that hidden cards are considered as empty cards which you can't either destroy or build on before they
        // are revealed.
        SaboteurTile[][] hiddenboard = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(this.board[i], 0, hiddenboard[i], 0, BOARD_SIZE);
        }
        for(int h=0;h<3;h++){
            if(turnPlayer==1 && !player1hiddenRevealed[h] || turnPlayer==0 && !player2hiddenRevealed[h]){
                hiddenboard[hiddenPos[h][0]][hiddenPos[h][1]] = new SaboteurTile("8");
            }
        }
        return hiddenboard;
    }
}
