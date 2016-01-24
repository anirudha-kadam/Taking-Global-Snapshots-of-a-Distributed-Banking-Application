import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class Controller {

	public static int snapshot_num = 0 ;
	public static List<BranchID> readFile(String fileName) {
		File file = new File(fileName);
		BufferedReader br = null;
		List<BranchID> all_branches = new ArrayList<BranchID>();
		try {
			br = new BufferedReader(new FileReader(file));
			String currentLine = null;
			while ((currentLine = br.readLine()) != null) {
				String splitLine[] = currentLine.split("\\s");
				all_branches.add(new BranchID(splitLine[0], splitLine[1], Integer.parseInt(splitLine[2])));
			}

		} catch (FileNotFoundException e) {
			System.err.println("File could not be found: " + e.getMessage());
			System.exit(0);
		} catch (IOException e) {
			System.err.println("Found IOException: " + e.getMessage());
			System.exit(0);
		}
		return all_branches;

	}

	public static void main(String[] args) {
		if(args.length != 2){
			System.err.println("Two arguments are expected!");
			System.exit(0);
		}
		int balance = 0;
		try {
			balance = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("balance should be an integer: "+e.getMessage());
			System.exit(0);
		}
		String fileName = args[1];
		List<BranchID> all_branches = readFile(fileName);
		int balancePerBranch = balance / all_branches.size();
		String operation;
		for (int i = 0; i < all_branches.size(); i++) {
			operation = "initBranch";
			TTransport transport;
			transport = new TSocket(all_branches.get(i).getIp(), all_branches.get(i).getPort());
			try {
				transport.open();
				TProtocol protocol = new TBinaryProtocol(transport);
				Branch.Client client = new Branch.Client(protocol);

				perform(client, balancePerBranch, all_branches, 0, operation);

				transport.close();
			} catch (TTransportException e) {
				System.err.println(e.getMessage());
				System.exit(0);
			} catch (TException e) {
				System.err.println(e.getMessage());
				System.exit(0);
			}

		}
		
		RetrieveController retrieveController = new RetrieveController(all_branches);
		Thread snapshotRetriever = new Thread(retrieveController);
		snapshotRetriever.start();
		while(true){
			
			BranchID randomBranchID = all_branches.get(new Random().nextInt(all_branches.size()));
			operation = "initSnapshot";
			TTransport transport;
			transport = new TSocket(randomBranchID.getIp(), randomBranchID.getPort());
			try {
				Thread.sleep(5000);
				transport.open();
				TProtocol protocol = new TBinaryProtocol(transport);
				Branch.Client client = new Branch.Client(protocol);
				Controller.snapshot_num = Controller.snapshot_num + 1;
				perform(client, 0, all_branches, Controller.snapshot_num,  operation);

				transport.close();
			} catch (TTransportException e) {
				System.err.println(e.getMessage());
				System.exit(0);
			} catch (TException e) {
				System.err.println(e.getMessage());
				System.exit(0);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
			
			
		}
		
	}

	private static void perform(Branch.Client client, int balance, List<BranchID> all_branches,int snapshot_num, String operation) throws TException {
		if(operation.equals("initBranch")){
			client.initBranch(balance, all_branches);
		}else if(operation.equals("initSnapshot")){
			client.initSnapshot(snapshot_num);
		}
		
	}

}
