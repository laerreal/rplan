package edu.real.cross;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {

	static private Pattern space = Pattern.compile("\\s+");

	public static boolean wordAt(String s, int i, int word[])
	{
		Matcher m = space.matcher(s);

		int prev_end = 0;

		while (m.find()) {
			int space_start = m.start();
			int space_end = m.end();

			/* if i points at space right after word, the word is returned. */
			if (prev_end <= i && i <= space_start) {
				word[0] = prev_end;
				word[1] = space_start;
				return true;
			}

			prev_end = space_end;
		}

		// last word
		/* If i points right at EOL, the last word is returned. */
		if (prev_end <= i && i <= s.length()) {
			word[0] = prev_end;
			word[1] = s.length();
			return true;
		}

		return false;
	}

	static public void main(String[] args)
	{
		int[] test = new int[2];

		wordAt("0123  6789", 3, test);
		wordAt("0123  6789", 4, test);
		wordAt("0123  6789", 5, test);
		wordAt("0123  6789", 6, test);
		wordAt("01234567", 0, test);
		wordAt(" 1234567", 5, test);
	}
}
