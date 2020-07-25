/**
 * This class stores a Character and an Integer in every object.
 * 
 * @author John Weingart, CS10, Fall 2018
 *
 */
public class ValueChar {
	private Character thisChar;
	private Integer frequency;
	
	public ValueChar(Character character, Integer integer)
	{
		thisChar = new Character(character);
		frequency = new Integer(integer);
	}
	
	public Integer getInt()
	{
		return frequency;
	}
	public Character getChar()
	{
		return thisChar;
	}
	public String toString()
	{
		return thisChar.toString()+": " + frequency.intValue();
	}
}
