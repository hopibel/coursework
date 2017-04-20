package cs123.mp1.ibelgaufts;

public class Utils {
	public static String join(String[] strings, String separator) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < strings.length; ++i) {
			sb.append(strings[i]);
			if(i < strings.length - 1)
				sb.append(separator);
		}
		return sb.toString();
	}
}
