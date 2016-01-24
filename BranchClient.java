import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Random;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class BranchClient implements Runnable {

	public static BranchHandler handler;

	public BranchClient(BranchHandler handlerIn) {
		handler = handlerIn;
	}

	@Override
	public void run() {

		while (true) {

			try {

				if (handler.all_branches != null) {

					Thread.sleep((new Random().nextInt(5) + 1) * 1000);
					BranchID randomBranchID = handler.all_branches
							.get(new Random().nextInt(handler.all_branches.size()));
					TTransport transport;
					transport = new TSocket(randomBranchID.ip, randomBranchID.port);
					transport.open();
					TProtocol protocol = new TBinaryProtocol(transport);
					Branch.Client client = new Branch.Client(protocol);
					//System.out.println("Balance is " + handler.balance);
					//System.out.print("Money sent to " + randomBranchID.getName() + " ");
					if (!handler.sendingMarkers)
						perform(client);
					transport.close();

				}
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

	private static void perform(Branch.Client client) throws TException {

		if (handler.balance > 0) {
			int randomAmount = (int) (handler.balance * (float) (new Random().nextInt(5) + 1) / 100);

			handler.balance = handler.balance - randomAmount;
			//System.out.print(randomAmount + "\n");
			client.transferMoney(new TransferMessage(handler.branchID, randomAmount));
			//System.out.println("Balance is " + handler.balance);

		}

	}

}
