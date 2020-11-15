package com.gitee.search.queue;

/**
 * 队列中的任务
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueueTask {

    public final static byte TYPE_REPOSITORY    = 0x01; //仓库
    public final static byte TYPE_ISSUE         = 0x02;
    public final static byte TYPE_PR            = 0x03;
    public final static byte TYPE_COMMIT        = 0x04;
    public final static byte TYPE_WIKI          = 0x05;
    public final static byte TYPE_CODE          = 0x06;
    public final static byte TYPE_USER          = 0x07;

    public final static byte OPT_ADD            = 0x01; //添加
    public final static byte OPT_UPDATE         = 0x02; //修改
    public final static byte OPT_DELETE         = 0x03; //删除

    private byte type;
    private byte opt;
    private String body;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getOpt() {
        return opt;
    }

    public void setOpt(byte opt) {
        this.opt = opt;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * TODO 任务转 JSON
     * @return
     */
    public String json() {
        return "JSON";
    }

    /**
     * 解析 JSON 为 Task
     * @param json
     * @return
     */
    public static QueueTask parse(String json) {
        QueueTask task = new QueueTask();
        return task;
    }

    public static void main(String[] args) {
    }

}
