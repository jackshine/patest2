package com.csswust.patest2.common;

/**
 * Created by 972536780 on 2018/4/23.
 */
public enum MonitorKey {
    JUDGE_RESPONSE_TIME("judge_response_time", "判题平均响应时间"),
    USERINFO_SELECT_CONDITION("userInfo_select_condition", "条件查询用户次数");

    private String key;
    private String title;

    public static MonitorKey getByKey(String key) {
        for (MonitorKey monitorKey : MonitorKey.values()) {
            if (monitorKey.getKey().equals(key)) {
                return monitorKey;
            }
        }
        return null;
    }

    MonitorKey(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}