package cn.jxufe.seedsystem.entity;

public class Message {

    private int code;      // 0 成功，1 失败
    private String msg;    // 提示信息
    private Object data;   // 附加数据

    public Message() {}

    public Message(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Message(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
