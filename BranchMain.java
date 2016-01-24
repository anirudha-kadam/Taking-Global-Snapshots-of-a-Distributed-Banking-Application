import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class BranchMain {

	public static volatile BranchHandler handler;

	public static Branch.Processor processor;

	public static int port;

	public static void main(String[] args) {
		
		if(args.length != 2){
			System.err.println("Two arguments are expected!");
			System.exit(0);
		}
		try {
			int portNum = Integer.parseInt(args[1]);
			handler = new BranchHandler(args[0], portNum);
			processor = new Branch.Processor(handler);
			port = handler.branchID.port;
			BranchServer branchServer = new BranchServer(port, processor);
			Thread serverThread = new Thread(branchServer);
			serverThread.start();

			BranchClient branchClient = new BranchClient(handler);
			Thread clientThread = new Thread(branchClient);
			clientThread.start();
			
		} catch (NumberFormatException x) {
			System.err.println("port number should be integer: "+x.getMessage());
			System.exit(0);
		} catch (Exception x) {
			System.err.println(x.getMessage());
			System.exit(0);
		}

	}

}
