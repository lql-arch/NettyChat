package config;

public class saveFile {
    private Long start;
    private String name;
    private String path;
    private String rep_id;//接收方
    private String send_id;//发送方

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRep_id() {
        return rep_id;
    }

    public void setRep_id(String rep_id) {
        this.rep_id = rep_id;
    }

    public String getSend_id() {
        return send_id;
    }

    public void setSend_id(String send_id) {
        this.send_id = send_id;
    }



}
