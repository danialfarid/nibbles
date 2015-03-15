package com.df.nibbles;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        HttpServletRequest httpReq = (HttpServletRequest) req;
        httpResp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        if (((HttpServletRequest) req).getHeader("Origin") != null) {
            String origin = ((HttpServletRequest) req).getHeader("Origin");
            if (origin.startsWith("http://localhost:")) {
                httpResp.setHeader("Access-Control-Allow-Origin", origin);
            }
        }
        if (httpReq.getMethod().equalsIgnoreCase("OPTIONS")) {
            httpResp.setHeader("Access-Control-Allow-Headers",
                    httpReq.getHeader("Access-Control-Request-Headers"));
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}