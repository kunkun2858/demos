package com.github.cxt.MySpring.cli;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;

/**
 * 备注 在eclpse中运行没效果
 * 参考zookeeper的启动命令
 * @date 2016年9月7日
 */
public class JlineCliMain {

	protected static final Map<String,String> commandMap = new HashMap<String,String>();
	
	static {
        commandMap.put("connect", "host:port");
        commandMap.put("close","");
        commandMap.put("create", "[-s] [-e] path data acl");
        commandMap.put("delete","path [version]");
        commandMap.put("rmr","path");
        commandMap.put("set","path data [version]");
        commandMap.put("get","path [watch]");
        commandMap.put("ls","path [watch]");
        commandMap.put("quit","");
    }
	
    static void usage() {
        System.err.println("CxtCliDemo -server host:port cmd args");
        for (String cmd : commandMap.keySet()) {
            System.err.println("\t"+cmd+ " " + commandMap.get(cmd));
        }
    }
	 
	
	public static void main(String[] args) throws IOException {
		ConsoleReader reader = new ConsoleReader();
		
		reader.addCompleter(new Completer() {
			
			@Override
			public int complete(String buffer, int cursor, List<CharSequence> candidates) {
				buffer = buffer.substring(0,cursor);
				for (String cmd : commandMap.keySet()) {
		            if (cmd.startsWith( buffer )) {
		                candidates.add(cmd);
		            }
		        }
				//可以加入自己的业务
				return buffer.lastIndexOf(" ")+1;
			}
		});
		
		String line;
        while ((line = (String)reader.readLine("caixt>")) != null) {
        	if(line.trim().equals("")){
        		continue;
        	}
        	MyCommandOptions opts = new MyCommandOptions(line);
            if (opts.cmdArgs.size() < 1) {
                usage();
                continue;
            }
            if (!commandMap.containsKey(opts.command)) {
                usage();
                continue;
            }
        	else if(line.startsWith("quit")){
        		 System.out.println("Quitting...");
                 System.exit(0);
        	}
        	System.out.println(opts);
        }
	}
	
	
	static class MyCommandOptions {

        private List<String> cmdArgs = null;
        private String command = null;
        
        MyCommandOptions(String cmdstring){
        	StringTokenizer cmdTokens = new StringTokenizer(cmdstring, " ");          
            String[] args = new String[cmdTokens.countTokens()];
            int tokenIndex = 0;
            while (cmdTokens.hasMoreTokens()) {
                args[tokenIndex] = cmdTokens.nextToken();
                tokenIndex++;
            }
            command = args[0];
            cmdArgs = Arrays.asList(args);
        }

		@Override
		public String toString() {
			return "MyCommandOptions [cmdArgs=" + cmdArgs + ", command="
					+ command + "]";
		}
        
	}
}
