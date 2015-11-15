package ExchangeActiveSync;

public enum EasFolderType {
    None,
    UserCreated,
    DefaultInbox,
    DefaultDrafts,
    DefaultDeleted,
    DefaultSent,
    DefaultOutbox,
    DefaultTasks,
    DefaultCalendar,
    DefaultContacts,
    DefaultNotes,
    DefaultJournal,
    UserCreatedMail,
    UserCreatedCalendar,
    UserCreatedContacts,
    UserCreatedTasks,
    UserCreatedjournal,
    UserCreatedNotes,
    UnknownFolder,
    RecipientInformationCache;

    public static EasFolderType valueOf(int type) {
        EasFolderType[] vals = values();
        if (type >= 0 && type < vals.length)
            return vals[type];
        return null;
    }
}
