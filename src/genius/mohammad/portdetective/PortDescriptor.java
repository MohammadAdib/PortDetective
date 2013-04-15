package genius.mohammad.portdetective;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PortDescriptor {
	
	private List<String> database;
	
	public PortDescriptor(InputStream stream) {
		database = readDatabase(stream);
	}
	
	private List<String> readDatabase(InputStream stream) {
        List<String> contents = new ArrayList<String>();
        try {
          BufferedReader input =  new BufferedReader(new InputStreamReader(stream));
          try {
        	  String line = null;
        	  while (( line = input.readLine()) != null) {
        		  contents.add(":" + line);
        	  }
          }
          finally {
        	  input.close();
          }
        }
        catch (IOException ex){
        	ex.printStackTrace();
        }
        return contents;
    }
	
	public String describePort(int p) {
		String port = Integer.toString(p);
		String description = "	- Description not available.";
		for (String s : database) {
			if(s.indexOf(":" + port + " -") != -1) {
				description = "	" + s.substring(s.indexOf(" -") + 1).trim();
			}
		}
		return description;
	}
}
