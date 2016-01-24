import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SnapShotMetadata {
	public volatile Map<String, Boolean> channelStatusMap = null;
	public volatile LocalSnapshot localSnapshot = null;
	public SnapShotMetadata(List<BranchID> alL_branches, LocalSnapshot localSnapshotIn) {
		localSnapshot = localSnapshotIn;
		if(channelStatusMap == null){
			channelStatusMap = new ConcurrentHashMap<String, Boolean>();
			for(int i = 0; i < alL_branches.size(); i++){
				channelStatusMap.put(alL_branches.get(i).getName(), new Boolean(false));
			}
		}
	}
	
}
