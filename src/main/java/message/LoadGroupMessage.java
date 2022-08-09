package message;

import client.normal.GroupChat_text;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LoadGroupMessage extends Message{

    private List<GroupChat_text> groupMessages ;
    private String uid;//自己的uid
    private String gid;
    private String groupName;
    private List<String> administrator;
    private List<String> members;
    private String group_master;//uid
    private String masterName;
    private Map<String,String> uidNameMap;
    private Map<String,Boolean> uidBanned;
    private String time;
    private int membersCount;
    private String LastTime;
    private boolean buildGroup = false;

    public List<GroupChat_text> getGroupMessages() {
        return groupMessages;
    }

    public void setGroupMessages(List<GroupChat_text> groupMessages) {
        this.groupMessages = groupMessages;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getAdministrator() {
        return administrator;
    }

    public void setAdministrator(List<String> administrator) {
        this.administrator = administrator;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getGroup_master() {
        return group_master;
    }

    public void setGroup_master(String group_master) {
        this.group_master = group_master;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public Map<String, String> getUidNameMap() {
        return uidNameMap;
    }

    public void setUidNameMap(Map<String, String> uidNameMap) {
        this.uidNameMap = uidNameMap;
    }

    public Map<String, Boolean> getUidBanned() {
        return uidBanned;
    }

    public void setUidBanned(Map<String, Boolean> uidBanned) {
        this.uidBanned = uidBanned;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public String getLastTime() {
        return LastTime;
    }

    public void setLastTime(String lastTime) {
        LastTime = lastTime;
    }

    public boolean isBuildGroup() {
        return buildGroup;
    }

    public void setBuildGroup(boolean buildGroup) {
        this.buildGroup = buildGroup;
    }

    @Override
    public int getMessageType() {
        return LoadGroupMessage;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
