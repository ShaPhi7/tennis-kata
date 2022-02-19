package tennis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

public final class TennisMatch {

	private static Scanner scanner = new Scanner(System.in);
	private static String inputFilePathAndName = "";
	private static String outputFilePathAndName = "";
	
	private final static String FILEPATH = "./data/tennis/";
	private final static String TXT_EXTENSION = ".txt";
	public final static String FILE_DEFAULT_INPUT = getFileString("input");
	public final static String FILE_DEFAULT_OUTPUT = getFileString("output");
	public final static String FILE_EXPECTED_OUTPUT = getFileString("expectedOutput");
	
	private final static char CHAR_PLAYER_A = 'A';
	private final static char CHAR_PLAYER_B = 'B';
	
	private final static int SET_WIN_REQUIREMENT_GAMES_WON = 6;
	private final static int SET_WIN_REQUIREMENT_GAMES_LEAD = 2;
	private final static int GAME_WIN_REQUIREMENT_POINTS_WON = 4;
	private final static int GAME_WIN_REQUIREMENT_POINTS_LEAD = 2;
	
	private String abStr = "";
	private List<String> scoreBoardPlayerA = new ArrayList<>();
	private List<String> scoreBoardPlayerB = new ArrayList<>();
	private int gamesCompleted = 0;
	
	public static void main(String[] args)
	{	
		setInputAndOutputFiles();
		outputFormattedScoresForInputAbStrings();
	}

	private static void setInputAndOutputFiles()
	{
		System.out.println("Enter input file name:");
		inputFilePathAndName = getFileString(scanner.nextLine());
		
		System.out.println("Enter output file name:");
		outputFilePathAndName = getFileString(scanner.nextLine());
	}

	public static void outputFormattedScoresForInputAbStrings()
	{
		Iterator<String> input = readInput();
		List<String> output = new ArrayList<>();
		
		while(input.hasNext())
		{
			String inputLine = input.next();

			TennisMatch tennisMatch = new TennisMatch(inputLine);
			tennisMatch.play();
			output.add(tennisMatch.score());
		}

		writeOutput(output);
	}

	public TennisMatch(String abStr)
	{
		this.abStr = abStr;
	}
	
	public void play()
	{
		List<Character> currentSetPoints = new ArrayList<>();
		List<Character> currentGamePoints = new ArrayList<>();
		
		for (int charIndex=0; charIndex<abStr.length(); charIndex++)
		{
			char abChar = abStr.charAt(charIndex);
			currentGamePoints.add(abChar);
			
			if (isGameCompleted(currentGamePoints))
			{
				completeGame(currentSetPoints, currentGamePoints);
				
				if (isSetCompleted(currentSetPoints))
				{
					completeSet(currentSetPoints);
				}
			}
		}
		
		recordIncompleteSet(currentSetPoints, currentGamePoints);
	}

	private void completeSet(List<Character> currentSetPoints)
	{
		recordGamesWonInSet(currentSetPoints);
		currentSetPoints.clear();
	}

	private void completeGame(List<Character> currentSetPoints, List<Character> currentGamePoints)
	{
		gamesCompleted++;
		
		char gameWinner = getGameWinner(currentGamePoints);
		currentSetPoints.add(gameWinner);
		
		currentGamePoints.clear();
	}
	
	private void recordIncompleteSet(List<Character> gameWinners, List<Character> gamePoints)
	{
		recordGamesWonInSet(gameWinners);
		recordPointsWonInGameIfAny(gamePoints);
	}

	private void recordPointsWonInGameIfAny(List<Character> gamePoints)
	{
		if (!gamePoints.isEmpty())
		{
			String scoreInOngoingGamePlayerA = getPlayerScoreInOngoingGame(gamePoints, CHAR_PLAYER_A);
			String scoreInOngoingGamePlayerB = getPlayerScoreInOngoingGame(gamePoints, CHAR_PLAYER_B);
			
			scoreBoardPlayerA.add(scoreInOngoingGamePlayerA);
			scoreBoardPlayerB.add(scoreInOngoingGamePlayerB);
		}
	}

	private boolean isGameCompleted(List<Character> points)
	{	
		return isCompleted(points, GAME_WIN_REQUIREMENT_POINTS_WON, GAME_WIN_REQUIREMENT_POINTS_LEAD);
	}
	
	private boolean isSetCompleted(List<Character> points)
	{	
		return isCompleted(points, SET_WIN_REQUIREMENT_GAMES_WON, SET_WIN_REQUIREMENT_GAMES_LEAD);
	}
	
	private boolean isCompleted(List<Character> points, int pointsNeededForWin, int gapNeededForWin) {
		
		int currentSetPointsForPlayerA = Collections.frequency(points, CHAR_PLAYER_A);
		int currentSetPointsForPlayerB = Collections.frequency(points, CHAR_PLAYER_B);
		
		boolean someoneScoredEnoughPoints = currentSetPointsForPlayerA >= pointsNeededForWin
								  		 || currentSetPointsForPlayerB >= pointsNeededForWin;
								  
		boolean someoneFarEnoughAhead = Math.abs(currentSetPointsForPlayerA - currentSetPointsForPlayerB)
										>= gapNeededForWin;
		
		return someoneScoredEnoughPoints
		  && someoneFarEnoughAhead;
	}

	public String score()
	{
		StringJoiner sjScore = new StringJoiner(" ");
		
		for (int i=0; i<scoreBoardPlayerA.size(); i++)
		{	
			if (gamesCompleted % 2 == 0)
			{
				sjScore.add(new StringBuilder()
						.append(scoreBoardPlayerA.get(i))
						.append("-")
						.append(scoreBoardPlayerB.get(i)));
			}
			else
			{
				sjScore.add(new StringBuilder()
						.append(scoreBoardPlayerB.get(i))
						.append("-")
						.append(scoreBoardPlayerA.get(i)));
			}
		}
		return sjScore.toString();
	}
	
	private void recordGamesWonInSet(List<Character> gameWinners)
	{
		int scorePlayerA = getpointsScoredForPlayer(gameWinners, CHAR_PLAYER_A);
		scoreBoardPlayerA.add(Integer.toString(scorePlayerA));
		
		int scorePlayerB = getpointsScoredForPlayer(gameWinners, CHAR_PLAYER_B);
		scoreBoardPlayerB.add(Integer.toString(scorePlayerB));
	}

	private char getGameWinner(List<Character> gameWinners)
	{
		if (getpointsScoredForPlayer(gameWinners, CHAR_PLAYER_A) > getpointsScoredForPlayer(gameWinners, CHAR_PLAYER_B))
		{
			return CHAR_PLAYER_A;
		}
		else
		{
			return CHAR_PLAYER_B;
		}
	}
	
	private String getPlayerScoreInOngoingGame(List<Character> gamePoints, char player)
	{
		GameScore gameScore = null;
		int pointsScored = getpointsScoredForPlayer(gamePoints, player);
		switch(pointsScored)
		{
		case 0:
			gameScore = GameScore.LOVE;
			break;
		case 1:
			gameScore = GameScore.FIFTEEN;
			break;
		case 2:
			gameScore = GameScore.THIRTY;
			break;
		case 3:
			gameScore = GameScore.FOURTY;
			break;
		case 4:
		case 5:
			gameScore = getGameScoreForPlayerInDeuceGame(gamePoints, player);
			break;
		}
		return gameScore.getScoreDescription();
	}

	private GameScore getGameScoreForPlayerInDeuceGame(List<Character> gamePoints, char player)
	{
		GameScore gameScore = null;
		
		if (getpointsScoredForPlayer(gamePoints, player) > getPointsLostForPlayer(gamePoints, player))
		{
			gameScore = GameScore.ADVANTAGE;
		}
		else
		{
			gameScore = GameScore.FOURTY;
		}
		
		return gameScore;
	}

	private int getPointsLostForPlayer(List<Character> gamePoints, char player)
	{
		return gamePoints.size() - getpointsScoredForPlayer(gamePoints, player);
	}
	
	private int getpointsScoredForPlayer(List<Character> gameWinners, char player)
	{
		return (int)gameWinners.stream()
							   .filter(ch -> ch==player)
							   .count();
	}
	
	private enum GameScore
	{
		LOVE("0"),
		FIFTEEN("15"),
		THIRTY("30"),
		FOURTY("40"),
		ADVANTAGE("A");
		
		private String scoreDescription;
		
		private GameScore(String scoreDescription)
		{
			this.scoreDescription = scoreDescription;
		}

		public String getScoreDescription()
		{
			return scoreDescription;
		}
	}
	
	private static String getFileString(String fileName)
	{
		return FILEPATH + fileName + TXT_EXTENSION;
	}
	
	//read/write
	public static Iterator<String> readExpectedOutput()
	{
		return readAllLinesFromGivenFileOrDefault("", FILE_EXPECTED_OUTPUT);
	}
	
	public static Iterator<String> readActualOutput()
	{
		return readAllLinesFromGivenFileOrDefault(outputFilePathAndName, FILE_DEFAULT_OUTPUT);
	}
	
	private static Iterator<String> readInput()
	{
		return readAllLinesFromGivenFileOrDefault(inputFilePathAndName, FILE_DEFAULT_INPUT);
	}
	
	private static Iterator<String> readAllLinesFromGivenFileOrDefault(String givenFilePathAndName, String defaultFilePathAndName)
	{
		List<String> lines = new ArrayList<>();
		
		try
		{
			if (!givenFilePathAndName.isEmpty())
			{
				lines = Files.readAllLines(Paths.get(givenFilePathAndName));
			}
			else
			{
				lines = Files.readAllLines(Paths.get(defaultFilePathAndName));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return lines.iterator();
	}
	
	private static void writeOutput(List<String> output)
	{
		try
		{
			if (!outputFilePathAndName.isEmpty())
			{
				Files.write(Paths.get(outputFilePathAndName), output);
			}
			else
			{
				Files.write(Paths.get(FILE_DEFAULT_OUTPUT), output);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
