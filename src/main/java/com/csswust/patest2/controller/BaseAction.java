package com.csswust.patest2.controller;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Map.Entry;

public class BaseAction {
    // 这样是线程安全的
    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired(required = false)
    private HttpServletResponse response;

    public void saveSession(HttpServletRequest request, Map<String, Object> param) {
        HttpSession session = request.getSession();
        for (Entry<String, Object> entry : param.entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void saveSession(HttpServletRequest request, String paramName, Object value) {
        HttpSession session = request.getSession();
        session.setAttribute(paramName, value);
    }

    public Object getSession(HttpServletRequest request, String paramName) {
        HttpSession session = request.getSession();
        return session.getAttribute(paramName);
    }

    public void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
    }

    public String getIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
