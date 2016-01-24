import java.net.InetAddress;

import java.net.UnknownHostException;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;

import javax.management.monitor.MonitorSettingException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class BranchHandler implements Branch.Iface {

	static volatile int balance = 0;
	static volatile List<BranchID> all_branches = null;
	static volatile BranchID branchID = null;
	static volatile Map<Integer, SnapShotMetadata> snapshotMap = null;
	static volatile boolean sendingMarkers;
	
	public BranchHandler(String nameIn, int portIn) {
		snapshotMap = new ConcurrentHashMap<Integer, SnapShotMetadata>();
		sendingMarkers = false;
		try {
			branchID = new BranchID(nameIn,
					InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()).toString().substring(1), portIn);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initBranch(int balance, List<BranchID> all_branches) throws SystemException, TException {
		this.balance = this.balance + balance;
		List<BranchID> listOfOtherBranches = new ArrayList<BranchID>();
		for (int i = 0; i < all_branches.size(); i++) {
			if (!all_branches.get(i).name.equals(branchID.name)) {
				listOfOtherBranches.add(all_branches.get(i));
			}
		}
		this.all_branches = new ArrayList<BranchID>(listOfOtherBranches);
		
	}

	@Override
	public void transferMoney(TransferMessage message) throws SystemException, TException {
		for (Map.Entry<Integer, SnapShotMetadata> entry : snapshotMap.entrySet()) {
			Integer snapshot_num = entry.getKey();
			SnapShotMetadata snapShotMetadata = entry.getValue();
			if (snapShotMetadata.channelStatusMap.get(message.getOrig_branchId().getName()).equals(false)) {
				snapShotMetadata.localSnapshot.messages.add(message.amount);
				snapshotMap.put(snapshot_num, snapShotMetadata);
			}

		}
		//System.out.println("balance is: "+this.balance);
		//System.out.println("Money"+ message.amount+" has come from "+message.orig_branchId.getName());
		balance = balance + message.amount;
		//System.out.println("balance is: "+this.balance);

	}

	@Override
	public void initSnapshot(int snapshot_num) throws SystemException, TException {
		sendingMarkers = true;
		//System.out.println("Snapsho " + snapshot_num + " initiated by " + branchID.name);
		if (snapshotMap.get(snapshot_num) == null) {
			LocalSnapshot latestLocalState = new LocalSnapshot(snapshot_num, this.balance, new ArrayList<Integer>());
			snapshotMap.put(snapshot_num, new SnapShotMetadata(all_branches, latestLocalState));
		}
		for (int j = 0; j < all_branches.size(); j++) {
			TTransport transport;
			transport = new TSocket(all_branches.get(j).getIp(),
					all_branches.get(j).getPort());
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			Branch.Client client = new Branch.Client(protocol);
			//System.out.println("Marker sent to "+all_branches.get(j).getName()+" for snapshot "+ snapshot_num);
			client.Marker(this.branchID, snapshot_num);
			transport.close();
		}
		sendingMarkers = false;

	}

	@Override
	public void Marker(BranchID branchId, int snapshot_num) throws SystemException, TException {
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			//System.err.println(e.getMessage());
		}
		//System.out.println("Marker has come from: " + branchId.getName() + " for snapshot: " + snapshot_num);
		if (snapshotMap.get(snapshot_num) == null) {
			sendingMarkers = true;
			LocalSnapshot latestLocalState = new LocalSnapshot(snapshot_num, this.balance, new ArrayList<Integer>());
			SnapShotMetadata snapShotMetadata = new SnapShotMetadata(all_branches, latestLocalState);
			snapShotMetadata.channelStatusMap.put(branchId.getName(), new Boolean(true));
			snapshotMap.put(snapshot_num, snapShotMetadata);
			//sendingMarkers = false;
			for (int j = 0; j < all_branches.size(); j++) {
				TTransport transport;
				transport = new TSocket(all_branches.get(j).getIp(),
						all_branches.get(j).getPort());
				transport.open();
				TProtocol protocol = new TBinaryProtocol(transport);
				Branch.Client client = new Branch.Client(protocol);
				//System.out.println("Marker sent to "+all_branches.get(j).getName()+" for snapshot "+ snapshot_num);
				client.Marker(this.branchID, snapshot_num);
				transport.close();
			}
			sendingMarkers = false;
			
		} else {
			SnapShotMetadata snapShotMetadata = snapshotMap.get(snapshot_num);
			snapShotMetadata.channelStatusMap.put(branchId.getName(), new Boolean(true));
			snapshotMap.put(snapshot_num, snapShotMetadata);
		}
		
		
	}

	@Override
	public LocalSnapshot retrieveSnapshot(int snapshot_num) throws SystemException, TException {
		
		LocalSnapshot localSnapshot = snapshotMap.get(snapshot_num).localSnapshot;
		snapshotMap.remove(snapshot_num);
		return localSnapshot;
	}

}
