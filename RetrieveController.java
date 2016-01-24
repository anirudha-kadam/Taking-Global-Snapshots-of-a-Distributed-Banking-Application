import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class RetrieveController implements Runnable{
	public static int snapshot_num = 0 ;
	List<BranchID> all_branches = null;
	public RetrieveController(List<BranchID> all_branchesIn) {
		all_branches = new ArrayList<BranchID>(all_branchesIn);
	}
	@Override
	public void run() {
		
		while(true){
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				System.err.println(e1.getMessage());
			}
			RetrieveController.snapshot_num = RetrieveController.snapshot_num + 1;
			for (int i = 0; i < all_branches.size(); i++) {
				
				TTransport transport;
				transport = new TSocket(all_branches.get(i).ip, all_branches.get(i).port);
				try {
					transport.open();
					TProtocol protocol = new TBinaryProtocol(transport);
					Branch.Client client = new Branch.Client(protocol);
					perform(client, RetrieveController.snapshot_num, all_branches.get(i).getName());

					transport.close();
				} catch (TTransportException e) {
					System.err.println(e.getMessage());
					System.exit(0);
				} catch (TException e) {
					System.err.println(e.getMessage());
					System.exit(0);
				}

			}
			System.out.println("__________________________________________________________________________________\n");

		}
		
	}
	
	
	private static void perform(Branch.Client client,int snapshot_num, String branchName) throws TException {
		
		LocalSnapshot localSnapshot = client.retrieveSnapshot(snapshot_num);
		System.out.println("Snapshot: "+snapshot_num+" for branch: "+branchName+"--> local balance: "+localSnapshot.getBalance()+" incoming channels: "+localSnapshot.getMessages());
	}

}
