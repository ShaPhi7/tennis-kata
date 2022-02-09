package tennis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class TennisMatchTest {

	@Rule public ErrorCollector collector = new ErrorCollector();
	
	@Test
	public void testTennisMatchUsingInputOutputFiles()
	{
		TennisMatch.outputFormattedScoresForInputAbStrings();

		Iterator<String> expectedOutput = TennisMatch.readExpectedOutput();
		Iterator<String> actualOutput = TennisMatch.readActualOutput();
		
		while (expectedOutput.hasNext()
		  && actualOutput.hasNext())
		{	
			collector.checkThat(expectedOutput.next(), is(actualOutput.next()));
		}
	}
	
	/**
	 * Test where game is presented as in row 13 but previous sets are present, and the completion of 
	 * the game causes the completion of a set i.e. test that 5-4 becomes 4-6 0-0, not 4-6, nor 4-6 0-0 0-0. 
	 */
	@Test
	public void testCompletedGameCausingCompletedSet()
	{
		TennisMatch tennisMatch = new TennisMatch("AAAABBBBAAAABBBBAAAABBBBAAAABBBBBBBBBBBAAAABBB");
		tennisMatch.play();
		String outputLine = tennisMatch.score();

		assertThat(outputLine, is("4-6 0-0"));
	}

	/**
	 * Test where two sets have been completed, but the current server is different at the start of each set
	 * (e.g. 3-6 6-3 0-0 0-15), specifically to check all scores are shown with current server first.
	 */
	@Test
	public void changeOfServerAtStartOfMultipleSets()
	{
		TennisMatch tennisMatch = new TennisMatch("AAAABBBBAAAABBBBAAAABBBBBBBBBBBBBBBBAAAABBBBAAAABBBBAAAABBBBAAAAAAAAAAAAB");
		tennisMatch.play();
		String outputLine = tennisMatch.score();

		assertThat(outputLine, is("3-6 6-3 0-0 0-15"));
	}
}
