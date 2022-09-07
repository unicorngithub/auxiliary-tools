package com.auxiliary.interfaces.log.advice;

import com.auxiliary.interfaces.log.config.AuxiliaryInterfacesLogProperties;
import com.auxiliary.interfaces.log.utils.AddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 辅助工具-启动打印日志
 *
 * @author Guo's
 */
@Slf4j
public class InterfacesRunLogAdvice {

    private final AuxiliaryInterfacesLogProperties properties;
    private final ApplicationContext application;

    public InterfacesRunLogAdvice(AuxiliaryInterfacesLogProperties properties, ApplicationContext application) {
        this.properties = properties;
        this.application = application;
    }

    public void printInterface() {
        try {
            Environment env = application.getEnvironment();
            String ip = AddressUtils.getLocalHostExactAddress().getHostAddress();
            String port = env.getProperty("server.port");
            String path = env.getProperty("server.servlet.context-path");
            String environmental = application.getEnvironment().getProperty("environmental.identification");
            if (StringUtils.isEmpty(path)) {
                path = "";
            }

            // 接口信息
            StringBuffer printInfo = new StringBuffer();
            printInfo.append("\n-------------------------------------------------------------------------\n\t" +
                    "Current operating environment: " + environmental + "\n\t" +
                    "Application is running! Access URLs:\n\t" +
                    "Local访问网址:  \t\thttp://localhost:" + port + path + "\n\t" +
                    "External访问网址: \thttp://" + ip + ":" + port + path + "\n\t" +
                    "-------------------------------------------------------------------------\n\t");
            // 获取所有链接
            printInfo.append("All request url:\n\t");
            Set<String> urls = getAllUrl(application);
            for (String url : urls) {
                printInfo.append("http://" + ip + ":" + port + path + url + "\n\t");
            }
            printInfo.append("Total:" + urls.size() + "\n");
            printInfo.append("-------------------------------------------------------------------\n");
            // 打印数据
            log.info(printInfo.toString());
        } catch (Exception ex) {
        }
    }

    // 获取项目所有url
    private Set<String> getAllUrl(ApplicationContext run) {
        RequestMappingHandlerMapping mapping = run.getBean(RequestMappingHandlerMapping.class);
        //获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        Set<String> urls = new HashSet<>();
        for (RequestMappingInfo info : map.keySet()) {
            //获取url的Set集合，一个方法可能对应多个url
            PathPatternsRequestCondition patternsCondition = info.getPathPatternsCondition();
            Set<String> patterns = patternsCondition.getDirectPaths();

            // 这里可获取请求方式 Get,Post等等
            // Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            for (String url : patterns) {
                urls.add(url);
            }
        }
        return urls;
    }

}
