package ExchangeActiveSync;

import java.util.ArrayList;
import java.util.List;

public class EasFolder {
	private String id;
	private String parentId;
	private String name;
	private EasFolderType type;

	private String syncKey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EasFolderType getType() {
		return type;
	}

	public void setType(EasFolderType type) {
		this.type = type;
	}

	public String getSyncKey() {
		return syncKey;
	}

	public void setSyncKey(String syncKey) {
		this.syncKey = syncKey;
	}

	public static List<EasFolder> FindByType(List<EasFolder> folders, EasFolderType type) {
		List<EasFolder> result = new ArrayList<>();
		for (EasFolder f : folders) {
			if (f.getType() == type)
				result.add(f);
		}
		return result;
	}

	public static List<EasFolder> FindByName(List<EasFolder> folders, String name) {
		List<EasFolder> result = new ArrayList<>();
		for (EasFolder f : folders) {
			if (f.getName().equals(name))
				result.add(f);
		}
		return result;
	}

}
