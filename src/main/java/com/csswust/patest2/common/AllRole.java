package com.csswust.patest2.common;

/**
 * Created by 972536780 on 2017/11/26.
 */
public enum AllRole {
    ADMIN(1, "admin"),
    TEACHER(2, "teacher"),
    STUDENT(3, "student"),
    NOT_LOGIN(4, "not_login");

    private int id;
    private String permisson;

    AllRole(int id, String permisson) {
        this.id = id;
        this.permisson = permisson;
    }

    public static AllRole getByName(String name) {
        for (AllRole role : AllRole.values()) {
            if (role.getPermisson().equals(name)) {
                return role;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPermisson() {
        return permisson;
    }

    public void setPermisson(String permisson) {
        this.permisson = permisson;
    }
}
