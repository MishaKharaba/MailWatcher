package ExchangeActiveSync;

public class EasSyncCommand {
    public enum Type {
        // This enumeration represents the types
        // of commands available.
        Invalid, Add, Change, Delete, SoftDelete
    }

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
