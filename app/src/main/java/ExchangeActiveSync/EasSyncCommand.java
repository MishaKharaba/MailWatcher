package ExchangeActiveSync;

import java.util.ArrayList;
import java.util.List;

public class EasSyncCommand {

    public enum Type {
        // This enumeration represents the types
        // of commands available.
        Invalid,
        Add,
        Change,
        Delete,
        SoftDelete;

    }

    private List<Command> added = new ArrayList<>();
    private List<Command> updated = new ArrayList<>();
    private List<Command> deleted = new ArrayList<>();
    private String syncKey;

    public Command add(String serverId, Type type) throws Exception {
        Command cmd = new Command();
        cmd.setId(serverId);
        cmd.setType(type);
        switch (type) {
            case Add:
                added.add(cmd);
                break;
            case Change:
                updated.add(cmd);
                break;
            case Delete:
                deleted.add(cmd);
                break;
            case SoftDelete:
                deleted.add(cmd);
                break;
            default:
                throw new Exception("Invalid sync command type: " + type.toString());
        }
        return cmd;
    }

    public List<Command> getAdded() {
        return added;
    }

    public List<Command> getUpdated() {
        return updated;
    }

    public List<Command> getDeleted() {
        return deleted;
    }

    public Command getLastAdded() {
        return getAdded().size() > 0 ? getAdded().get(getAdded().size() - 1) : null;
    }

    public String getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(String syncKey) {
        this.syncKey = syncKey;
    }

    public int allSize() {
        return added.size() + updated.size() + deleted.size();
    }

    public class Command {
        private String id;
        private Type type;
        private EasMessage message;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public EasMessage getMessage() {
            return message;
        }

        public void setMessage(EasMessage message) {
            this.message = message;
        }
    }
}