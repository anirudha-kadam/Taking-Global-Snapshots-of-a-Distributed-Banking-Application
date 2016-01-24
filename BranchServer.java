import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;

import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TThreadPoolServer;

public class BranchServer implements Runnable{
	

	public static Branch.Processor processor;

	public static int port;
	
	public BranchServer(int portIn, Branch.Processor processorIn) {
		port = portIn;
		processor = processorIn;
	}
	
	@Override
	public void run() {
		simple(processor);
		
	}
	
	public static void simple(Branch.Processor processor) {
	    try {
	    //TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
	    //TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport).processor(processor));
	      TServerTransport serverTransport = new TServerSocket(port);
	      TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
	      System.out.println("Starting Branch server...");
	      server.serve();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

}
