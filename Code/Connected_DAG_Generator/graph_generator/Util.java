package graph_generator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

public class Util {
	public static int intFromRange(int min, int max){
		 return min + ((int) ((max - min + 1) * Math.random()));
	}
	public static String nodeSetToString(Set<Node> set){
		StringBuilder builder = new StringBuilder();
			boolean first = true;
			for(Node outgoingNode: set){
				if(first){
					first = false;
				}else{
					builder.append(GraphConfig.ARRAY_SEPERATOR);
				}
				builder.append(outgoingNode.getId());
			}
		return builder.toString();
	}
	
	public static String currentTime(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
}
