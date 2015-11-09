package ExchangeActiveSync;

public class FolderInfo {
	public enum Action {
		None, Added, Deleted, Updated
	}

	public String id;
	public String parentId;
	public String name;
	public int type;
	public Action action;
}
